#
# spec file for package projectlibre
#
Summary: ProjectLibre
Name: projectlibre
Version: @version@
Release: @rpm_revision@
License: CPAL
Group: Applications/Office
URL: http://www.projectlibre.com
Vendor: Projity
Packager: Laurent Chretienneau, Howard Katz
Prefix: /usr/share/projectlibre
BuildArchitectures: noarch
Requires: jre >= 1.5.0
Requires(post): desktop-file-utils
Requires(post): shared-mime-info
Requires(postun): desktop-file-utils
Requires(postun): shared-mime-info

%description
A desktop replacement for Microsoft Project. It is capable of sharing files with Microsoft Project and has very similar functionality (Gantt, PERT diagram, histogram, charts, reports, detailed usage), as well as tree views which aren't in MS Project.

%prep

%build

%install


%post
update-desktop-database &> /dev/null || :
update-mime-database /usr/share/mime &> /dev/null || :

%postun
update-desktop-database &> /dev/null || :
update-mime-database /usr/share/mime &> /dev/null || :

%files
/usr/share/projectlibre
/usr/bin/projectlibre
/usr/share/icons/projectlibre.png
/usr/share/applications/projectlibre.desktop
/usr/share/mime/packages/projectlibre.xml
