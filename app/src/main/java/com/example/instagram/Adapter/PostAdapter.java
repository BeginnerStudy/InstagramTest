package com.example.instagram.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.CommentsActivity;
import com.example.instagram.Fragment.PostDetailFragment;
import com.example.instagram.Fragment.ProfileFragment;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    public Context context;
    public List<Post> posts;

    FirebaseUser firebaseUser;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = posts.get(position);

        Glide.with(context).load(post.getPostimage()).into(holder.post_image);

        if (post.getDescription().equals("")){
            holder.description.setVisibility(View.GONE);
        }else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
        }

        publisherInfo(holder.image_profile, holder.username, holder.publisher, post.getPublisher());
        isLiked(post.getPostid(), holder.like);
        nrLikes(holder.likes, post.getPostid());
        getComments(post.getPostid(), holder.comments);
        isSaved(post.getPostid(), holder.save);

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.framLayout_container,
                        new ProfileFragment()).commit();
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.framLayout_container,
                        new ProfileFragment()).commit();
            }
        });

        holder.publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.framLayout_container,
                        new ProfileFragment()).commit();
            }
        });

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();

                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.framLayout_container,
                        new PostDetailFragment()).commit();
            }
        });

        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.save.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).removeValue();
                }
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.like.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("點讚數").child(post.getPostid())
                            .child(firebaseUser.getUid()).setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("點讚數").child(post.getPostid())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommentsActivity.class);
                Log.d("D100=",post.getPostid());
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                context.startActivity(intent);
            }
        });

        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile, post_image, like, comment, save;
        public TextView username, likes, publisher, description, comments;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.likes);
            publisher = itemView.findViewById(R.id.pulisher);
            description = itemView.findViewById(R.id.description);
            comments = itemView.findViewById(R.id.comments);
        }
    }

    private void getComments(String postid, final TextView comments){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("留言")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments.setText("查看 "+dataSnapshot.getChildrenCount()+" 則留言");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isLiked(String postid, final ImageView imageView){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("點讚數")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                }else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void nrLikes(final TextView likes, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("點讚數")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount()+" 個喜歡");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void publisherInfo(final ImageView image_profile, final TextView username, final TextView publisher, String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(context).load(user.getImageurl()).into(image_profile);
                username.setText(user.getUsername());
                publisher.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isSaved(final String postid, final ImageView imageView){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.ic_save_black);
                    imageView.setTag("saved");
                }else {
                    imageView.setImageResource(R.drawable.ic_savee_black);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
