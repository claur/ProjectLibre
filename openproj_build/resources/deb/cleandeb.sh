
GNU_TAR="$1"
P="$2"
DEB="$3"
DEB2="$4"
cd "$P"
mkdir -p "$P"/tmp
cd "$P"/tmp
ar -x "$P"/"$DEB"
"${GNU_TAR}" -xzvf control.tar.gz
"${GNU_TAR}" -xJvf data.tar.xz
"${GNU_TAR}" --numeric-owner --owner=0 --group=0 -czvf control.tar.gz control postinst postrm
"${GNU_TAR}" --numeric-owner --owner=0 --group=0 -cJvf data.tar.xz usr
ar -r "$P"/"$DEB2" debian-binary control.tar.gz data.tar.xz
cd ..
rm -rf tmp