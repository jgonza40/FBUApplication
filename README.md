# FBUApplication

# mem-recap

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
1. [Schema](#Schema)

## Overview
### Description
Social Media App with a focus on uploading memories in the form of images or quotes based on the location (city/country) they were created. Each pin on the map will be a place where the user created memories. This application then takes those memories and allows you to display a recap of your memories posted on the app thus far.

### App Evaluation
- **Category:** Social Networking
- **Mobile:** On the go! Most people are always carrying their phone; it will be easy for people to take a picture and upload it to add to their collection of memories.
- **Story:** Will allow friends to see the memories that their friends created throughout the year. It is a bit more intimate than other social media platforms (most users will probably only add their closest friends).
- **Market:** A way for individuals to keep in contact with close friends and family who they do not see frequently.
- **Habit:** Individuals create memories every day. They will feel driven to add more memories in hopes of having a better and fuller recap.
- **Scope:** There are a lot of small moving parts but I feel as though this app will be feasible. Creating an end of the year video of memories can be a stretch goal, but displaying memories in the form of a recap on the app will be feasible.

## Product Spec
### 1. User Stories (Required and Optional)

**Required Must-have Stories**
* User logs in to access feed of the memories created by users they follow
* User can request to add more friends when searching
* Google Maps is implemented with the ability to drop pins
* Map pins are clickable and lead to the display of images/quotes when clicking "view" 
  * Ability to add images/quotes when clicking "add"
* Recap button in user profile leads to a recap of images/quotes in a categorized manner


**Optional Nice-to-have Stories**

* Displaying people user follows' memory pins on the Google Maps API.
* Displaying a recap video based on memories (probably the trickiest)
* Asking for user's birthday when signing up to later display zodiac sign in profile

### 2. Screen Archetypes

* Login 
* Register - User signs up or logs into their account
   * Upon Download/Reopening of the application, the user is prompted to log in to gain access to their profile information to be properly matched with another person. 
   * ...
* Messaging Screen - Chat for users to communicate (direct 1-on-1)
   * Upon selecting music choice users matched and message screen opens
* Profile Screen 
   * Allows user to upload a photo and fill in information that is interesting to them and others
* Song Selection Screen.
   * Allows user to be able to choose their desired song, artist, genre of preference and begin listening and interacting with others.
* Settings Screen
   * Lets people change language, and app notification settings.



## Wireframes
<img src="images/wireframes.png" width=800><br>

## Schema 
### Models
#### Post

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | author        | Pointer to User| image author |
   | image         | File     | image that user posts |
   | caption       | String   | image caption by author |
   | commentsCount | Number   | number of comments that has been posted to an image |
   | likesCount    | Number   | number of likes for the post |
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |
### Networking
#### List of network requests by screen
   - Home Feed Screen
      - (Read/GET) Query all posts where user is author
         ```swift
         let query = PFQuery(className:"Post")
         query.whereKey("author", equalTo: currentUser)
         query.order(byDescending: "createdAt")
         query.findObjectsInBackground { (posts: [PFObject]?, error: Error?) in
            if let error = error { 
               print(error.localizedDescription)
            } else if let posts = posts {
               print("Successfully retrieved \(posts.count) posts.")
           // TODO: Do something with posts...
            }
         }
         ```
      - (Create/POST) Create a new like on a post
      - (Delete) Delete existing like
      - (Create/POST) Create a new comment on a post
      - (Delete) Delete existing comment
   - Create Post Screen
      - (Create/POST) Create a new post object
   - Profile Screen
      - (Read/GET) Query logged in user object
      - (Update/PUT) Update user profile image
#### [OPTIONAL:] Existing API Endpoints
##### An API Of Ice And Fire
- Base URL - [http://www.anapioficeandfire.com/api](http://www.anapioficeandfire.com/api)

   HTTP Verb | Endpoint | Description
   ----------|----------|------------
    `GET`    | /characters | get all characters
    `GET`    | /characters/?name=name | return specific character by name
    `GET`    | /houses   | get all houses
    `GET`    | /houses/?name=name | return specific house by name

##### Game of Thrones API
- Base URL - [https://api.got.show/api](https://api.got.show/api)

   HTTP Verb | Endpoint | Description
   ----------|----------|------------
    `GET`    | /cities | gets all cities
    `GET`    | /cities/byId/:id | gets specific city by :id
    `GET`    | /continents | gets all continents
    `GET`    | /continents/byId/:id | gets specific continent by :id
    `GET`    | /regions | gets all regions
    `GET`    | /regions/byId/:id | gets specific region by :id
    `GET`    | /characters/paths/:name | gets a character's path with a given name
