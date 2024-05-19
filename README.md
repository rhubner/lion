# Lion
_The storage app_

This app allows user to upload and download file. Files Uploaded with permission PUBLIC can be downloaded by other users.
It doesn't allow anonymous download of public files.

### Running application

The easy option is to use prepare docker compose file.

```bash
docker-compose -f stack.yml up
```
To remove stack :

```bash
docker-compose -f stack.yml down
```

## API descriptions

All api endpoint are secured with http basic authorisation.

## File API

### Put file

HTTP: `PUT`

URL: `/file/{filename}`

Http headers : 
 * `x-visibility` (Mandatory) - Valid options `PUBLIC`, `PRIVATE`.
 * `x-tags` (optional) - Up to 4 tags comma separated. For example: `picture,holiday,dubai`
 * `Content-Type` (Optional) — User defined content type. If not specified, a system will try to detect a content type based on the content.

Example request : 

```Bash
$ curl -X PUT -u radek:password -H 'x-visibility: PUBLIC' -H 'x-tags: picture,holiday,dubai' -T src/test/resources/img.lossless http://localhost:8080/file/img.lossless
{"url":"http://localhost:8080/file/img.lossless"}
```

### Get file

HTTP: `GET`

URL: `/file/{filename}`

Custom implemented HTTP response headers : 
* `x-tags`(Optional) - Return tags associated with file.
* `Content-Type` - User provided or autodetected content type

Example request (Ouput redacted) :

```Bash
$ curl -u radek:password -o /dev/null -v http://localhost:8080/file/img.lossless
> GET /file/img.lossless HTTP/1.1
> Host: localhost:8080
> Authorization: Basic cmFkZWs6cGFzc3dvcmQ=
> User-Agent: curl/7.85.0
> Accept: */*
>
< HTTP/1.1 200
< Vary: Origin
< Vary: Access-Control-Request-Method
< Vary: Access-Control-Request-Headers
< x-tags: picture,holiday,dubai
< X-Content-Type-Options: nosniff
< X-XSS-Protection: 0
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Frame-Options: DENY
< Content-Type: image/png
< Content-Length: 136478
< Date: Sun, 19 May 2024 07:08:09 GMT
<
* Connection #0 to host localhost left intact
```
### Rename file

HTTP: `PATCH`

URL: `/file/{filename}`

Body: Json : 
```Json
{"newFileName": "<new file name>"}
```

Example(Redacted) : 
```bash
$ curl -vvv -u radek:password -X PATCH --data '{"newFileName": "image.png"}' -H "content-type: application/json" http://localhost:8080/file/img.lossless
*   Trying 127.0.0.1:8080...
> PATCH /file/img.lossless HTTP/1.1
> content-type: application/json
> Content-Length: 28
>
< HTTP/1.1 200
<
"OK"* Connection #0 to host localhost left intact
```
### Delete file

HTTP: `DELETE`

URL: `/file/{filename}`

Example(Redacted) : 
```bash
$ curl -v -u radek:password -X DELETE http://localhost:8080/file/image.png
> DELETE /file/image.png HTTP/1.1
>
< HTTP/1.1 200
<
"OK"
```
## List API

HTTP: `GET`

URL: `/list`

Query parameters : 
* `visibility`(Mandatory) - `PUBLIC` - List all public files. `PRIVATE` - List all public and private files
* `tags` - Filter by tag. Maximum one tag. Example: `picture`
* `sortBy` - Available options : `name`, `uploadDate`, `tag`, `contentType`, `size`
* `page` - 0(Default) - N. 

Example :

List all private files with tag _dubai_
```bash
$ curl -u user:password 'http://localhost:8080/list?visibility=PRIVATE&tags=dubai'
```

List all private and public files order by name
```bash
$ curl -u user:password 'http://localhost:8080/list?visibility=PRIVATE&sortBy=name&page=1'
```

## Implementation notes

This application is not perfect. It was created as a demonstration only in time pressure. Some ideas what can be improved 
* Duplicate file detection. In *VERY* rare case, sha256 checksum may not be enough to detect duplicate files. 
  To be 100% sure, we may need to compare the content of the files.
* Error reporting. At the moment, I focus only on _happy path_ and didn't set up properly error management. 
  The Application should be able to return proper error, so that user knows what went wrong.
* Database model and indexes. At the moment, we have only a few indexes, and they even don't help too much with queries.
* Transaction over multiple sources. We store file, and then store data in a database. This is interaction with two data sources.
  If one fails, we should be able to recover from that state. For example, we should not leave file on filesystem when we detect duplicate file.
* Authentication - Http basic is not the best type of authentication. Secret token or oAuth will be better.
* Better testing. At the moment application contains minimum amount of test. Probably there are some errors that 
  can be easily detected with more unite/integration testing.
* Split between database model and presentation model. At the moment `list` endpoint return same data as we have in data.
* Use ID for all other interaction. Because stored file can be renamed, I decided to use UUID as _id_ in database and also name of the file on filesystem.
  Maybe it will be better to use this ID also for rename, delete, get. On other had, one requirments is that name is unique,
  this allows us to use name as identifier, and it's probably easier for user.
* better validation and wired range of input data. I simplify my implementation to only allow [a-z0-9] characters for all possible inputs.
  I think the system should be able to work with all kinds of UNICODE characters. Maybe even treat them as binary data.
* Use [DROID](https://github.com/digital-preservation/droid) from [The National Archives](https://www.nationalarchives.gov.uk/) for mime type detection.
  I spend a lot of time [improving this tool](https://github.com/digital-preservation/droid/pulls?q=is%3Apr+author%3Arhubner). 
  Unfortunately, it's not easy to integrate as Apache Tika. I was working on easy integration, but the National archives didn't 
  get money from the budget to continue.
