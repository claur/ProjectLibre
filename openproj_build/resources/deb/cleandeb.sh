
P="$1"
DEB="$2"
DEB2="$3"
cd "$P"
mkdir -p "$P"/tmp
cd "$P"/tmp
ar -x "$P"/"$DEB"
gnutar -xzvf control.tar.gz
gnutar -xzvf data.tar.gz
gnutar --numeric-owner --owner=0 --group=0 -czvf control.tar.gz control postinst postrm
gnutar --numeric-owner --owner=0 --group=0 -czvf data.tar.gz usr
ar -r "$P"/"$DEB2" debian-binary control.tar.gz data.tar.gz
cd ..
rm -rf tmp