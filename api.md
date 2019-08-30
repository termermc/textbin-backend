# API Documentation
All API paths should be prefixed with the appropriate API prefix. The prefix is always `/api/v<version>`, replacing `<version>` with the backend API version.

## Required Headers
All requests must be sent with both an `Origin` and a `Host` header.

`Origin` must be the normal site frontend origin. So if the frontend is located at `https://textbin.termer.net/`, then your `Origin` header should be `https://textbin.termer.net`. (Note that if using a non-standard port, you must also include it in your origin)

`Host` must be the host of the API you're hitting. If the API is located at `https://textbin.termer.net/`, then your `Host` header must be `textbin.termer.net`. As is with `Origin`, you must include the API port if it's non-standard.

## Cookies
For authentication, be sure to send the `vertx-web.session` cookie that is sent along with API responses, specifically after sucessfully using the `/auth` route.

## Basic API Response
All API responses will look somewhat like the example below:
```json
{
    "status": "success",
}
```
Every response includes the field `status`, which will either be `success`, or `error`. If it happens to be `error`, a field named `error` will be provided, including the error message. Example:
```json
{
    "status": "error",
    "error": "Database error"
}
```
Other than this, API responses will greatly differ based on what data they are designed to return.
For routes requiring a logged in user, the following will be returned if the minimum rank requirement is not met:
```json
{
    "status": "error",
    "error": "Access denied"
}
```


## Post Routes
GET `/public_posts` - Fetches public posts organized by last bump (descending)

* Minimum rank required: 0
 * Output:
    * posts (array)
        * name (string) - Post name/title
        * date (string) - Creation date in MM/DD/YYYY format
        * time (string) - Creation time in HH:MM format
        * category (int) - The ID of the category in which this post was created
        * post_id (string) - The ten-character post ID
        * category_code (string) - The 1-4 character code for the category this post is in
        * category_name (string) - The name of the category this post is in
        * comment_count (int) - The amount of comments on this post

GET `/latest_posts` - Fetches public posts organized by latest (descending)

 * Minimum rank required: 0
 * Output:
    * status (string) - The response status. success|error
    * posts (array)
        * name (string) - Post name/title
        * date (string) - Creation date in MM/DD/YYYY format
        * time (string) - Creation time in HH:MM format
        * category (int) - The ID of the category in which this post was created
        * sticky (int) - `1` if the post is sticky, `0` otherwise
        * post_id (string) - The ten-character post ID
        * category_code (string) - The 1-4 character code for the category this post is in
        * category_name (string) - The name of the category this post is in
        * comment_count (int) - The amount of comments on this post

GET `/get_post` - Gets info about a post

 * Minimum rank required: 0
 * Parameters:
     * id (string) - The post's ten character ID
 * Output:
    * status (string) - The response status. success|error
    * name (string) - The post name/title
    * type (string) - The post type. plain|markdown|html
    * text (string) - The post's content
    * date (string) - Creation date in MM/DD/YYYY format
    * time (string) - Creation time in HH:MM format
    * sticky (int) - `1` if the post is sticky, `0` otherwise
    * category (int) - The ID of the category in which this post was created
    * category_name (string) - The name of the category this post is in
    * category_code (string) - The 1-4 character code for the category this post is in

POST `/post` - Creates a new post

 * Minimum rank required: 0
 * Parameters:
    * text (string) - The post text content
    * category (int) - The category ID for this post (-1 for private)
    * name (string) - The post name/title
    * type (string) - The post type. plain|markdown|html
 * Output:
    * status (string) - The response status. success|error
