CREATE USER profile_user WITH PASSWORD 'profile-password';
CREATE DATABASE profile_db OWNER profile_user;

CREATE USER training_user WITH PASSWORD 'training-password';
CREATE DATABASE training_db OWNER training_user;
