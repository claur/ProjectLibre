#
# spec file for package ProjectLibre
#

#avoid default /usr/local/... when building on Mac 
%define _datadir /usr/share
%define _bindir /usr/bin

%define _target Linux-i386
%define _target_cpu i386
%define _target_os Linux

%define _topdir @basedir@/rpm_mageia
%define _tmppath @basedir@/rpm_mageia


Summary: ProjectLibre
Name: projectlibre
Version: @version@
Release: @rpm_revision@
License: CPAL
Group: Office
URL: http://www.projectlibre.org
Vendor: ProjectLibre
Packager: Laurent Chretienneau
BuildArchitectures: noarch
Requires: jre >= 1.6.0
Requires(post): desktop-file-utils
Requires(post): shared-mime-info
Requires(postun): desktop-file-utils
Requires(postun): shared-mime-info
BuildRoot: %{_topdir}/INSTALL 

%description
A desktop replacement for Microsoft Project. It is capable of sharing files with Microsoft Project and has very similar functionality (Gantt, PERT diagram, histogram, charts, reports, detailed usage), as well as tree views which aren't in MS Project.

%prep

%build

%install
echo %{_topdir}
echo $RPM_BUILD_ROOT
echo %{_bindir}
mkdir -p $RPM_BUILD_ROOT/%{_bindir}
cp -rf %{_sourcedir}/usr/bin/* $RPM_BUILD_ROOT/%{_bindir}/
mkdir -p $RPM_BUILD_ROOT/%{_datadir}
cp -rf %{_sourcedir}/usr/share/* $RPM_BUILD_ROOT/%{_datadir}/

%post
update-desktop-database &> /dev/null || :
update-mime-database %{_datadir}/mime &> /dev/null || :

%postun
update-desktop-database &> /dev/null || :
update-mime-database %{_datadir}/mime &> /dev/null || :

%files
%defattr(-,root,root)
%{_datadir}/projectlibre
%{_bindir}/projectlibre
%{_datadir}/pixmaps/projectlibre.png
%{_datadir}/applications/projectlibre.desktop
%{_datadir}/mime/packages/projectlibre.xml
