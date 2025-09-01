curl 'http://localhost:8080/file-storage' \
-H 'Content-Type: multipart/form-data; boundary=---WebAppBoundary' \
-H 'User-Agent: my-pc' \
-H 'X-Correlation-ID: corId' \
--data-binary $'-----WebAppBoundary\r
Content-Disposition: form-data; name="file"; filename="testfile.txt"\r
Content-Type: text/plain\r
X-File-ID: 1\r
X-File-Hash: 3d58a719c6866b0214f96b0a67b37e51a91e233ce0be126a08f35fdf4c043c6126f40139bfbc338d44eb2a03de9f7bb8eff0ac260b3629811e389a5fbee8a894\r
\r
Hello World\r
-----WebAppBoundary--\r
'
