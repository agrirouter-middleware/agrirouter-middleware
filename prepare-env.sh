# THIS SCRIPT COPIES .env.example TO .env, IF IT DOESN'T EXIST YET
# THEN IT REPLACES ALL PASSWORD PLACEHOLDERS BY NEW, STRONG PASSWORDS

secure_password() {
  < /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c${1:-16}
  echo
}

if [ -f .env ]; then
    echo Not overwriting exiting .env, still trying to set passwords...
else
    cp .env.example .env
fi

PLACEHOLDER=___CHANGEME___

grep $PLACEHOLDER .env > /dev/null
while [ $? -eq 0 ]; do
    echo Generating one new password...
    sed -i "0,/$PLACEHOLDER/{s/$PLACEHOLDER/$(secure_password)/}" .env
    grep $PLACEHOLDER .env > /dev/null
done
