for /R %%G in (..\assets\materials\*.mat) do matc --optimize-size --platform=mobile -o "%%~nG.filamat" "%%G"