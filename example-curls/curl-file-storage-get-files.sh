curl -v -X GET 'http://localhost:8080/file-storage' \
-H 'Content-Type: application/json' \
-H 'User-Agent: my-pc' \
-H 'X-Correlation-ID: corId' \
-d '{"directoryId":"328a145a-a230-4931-8813-e6469d2f1f09","files":[{"id":"1","fileId":"71bead9f-5764-4e88-93b5-1b674a6a37f9"}]}'
