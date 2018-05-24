/*
The contents of this file are subject to the Common Public Attribution License
Version 1.0 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.projity.com/license . The License is based on the Mozilla Public
License Version 1.1 but Sections 14 and 15 have been added to cover use of
software over a computer network and provide for limited attribution for the
Original Developer. In addition, Exhibit A has been modified to be consistent
with Exhibit B.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
specific language governing rights and limitations under the License. The
Original Code is OpenProj. The Original Developer is the Initial Developer and
is Projity, Inc. All portions of the code written by Projity are Copyright (c)
2006, 2007. All Rights Reserved. Contributors Projity, Inc.

Alternatively, the contents of this file may be used under the terms of the
Projity End-User License Agreeement (the Projity License), in which case the
provisions of the Projity License are applicable instead of those above. If you
wish to allow use of your version of this file only under the terms of the
Projity License and not to allow others to use your version of this file under
the CPAL, indicate your decision by deleting the provisions above and replace
them with the notice and other provisions required by the Projity  License. If
you do not delete the provisions above, a recipient may use your version of this
file under either the CPAL or the Projity License.

[NOTE: The text of this license may differ slightly from the text of the notices
in Exhibits A and B of the license at http://www.projity.com/license. You should
use the latest text at http://www.projity.com/license for your modifications.
You may not remove this license text from the source files.]

Attribution Information: Attribution Copyright Notice: Copyright (c) 2006, 2007
Projity, Inc. Attribution Phrase (not exceeding 10 words): Powered by OpenProj,
an open source solution from Projity. Attribution URL: http://www.projity.com
Graphic Image as provided in the Covered Code as file:  openproj_logo.png with
alternatives listed on http://www.projity.com/logo

Display of Attribution Information is required in Larger Works which are defined
in the CPAL as a work which combines Covered Code or portions thereof with code
not governed by the terms of the CPAL. However, in addition to the other notice
obligations, all copies of the Covered Code in Executable and Source Code form
distributed must, as a form of attribution of the original author, include on
each user interface screen the "OpenProj" logo visible to all users.  The
OpenProj logo should be located horizontally aligned with the menu bar and left
justified on the top left of the screen adjacent to the File menu.  The logo
must be at least 100 x 25 pixels.  When users click on the "OpenProj" logo it
must direct them back to http://www.projity.com.
*/
package com.projity.menu;

public interface MenuActionConstants {
	public static final String ACTION_NEW_PROJECT             = "NewProject";
	public static final String ACTION_OPEN_PROJECT            = "OpenProject";
	public static final String ACTION_IMPORT_MSPROJECT        = "ImportMSProject";
	public static final String ACTION_EXPORT_MSPROJECT        = "ExportMSProject";
	public static final String ACTION_CLOSE_PROJECT           = "CloseProject";
	public static final String ACTION_SAVE_PROJECT            = "SaveProject";
	public static final String ACTION_SAVE_PROJECT_AS         = "SaveProjectAs";
	public static final String ACTION_PRINT                   = "Print";
	public static final String ACTION_PRINT_PREVIEW           = "PrintPreview";
	public static final String ACTION_PDF         			  = "PDF";
	public static final String ACTION_EXIT                    = "Exit";

	public static final String ACTION_UNDO                    = "Undo";
	public static final String ACTION_REDO                    = "Redo";
	public static final String ACTION_CUT                     = "Cut";
	public static final String ACTION_COPY                    = "Copy";
	public static final String ACTION_PASTE                   = "Paste";

	public static final String ACTION_FILL_DOWN               = "FillDown";
	public static final String ACTION_FILL_RIGHT              = "FillRight";
	public static final String ACTION_FILL_UP                 = "FillUp";
	public static final String ACTION_FILL_LEFT               = "FillLeft";

	public static final String ACTION_CLEAR_ALL               = "ClearAll";
	public static final String ACTION_CLEAR_FORMATS           = "ClearFormats";
	public static final String ACTION_CLEAR_CONTENTS          = "ClearContents";
	public static final String ACTION_CLEAR_NOTES             = "ClearNotes";
	public static final String ACTION_CLEAR_HYPERLINKS        = "ClearHyperlinks";
	public static final String ACTION_CLEAR_ENTIRE            = "ClearEntire";
	public static final String ACTION_DELETE                  = "Delete";

	public static final String ACTION_LINK                    = "Link";
	public static final String ACTION_UNLINK                  = "Unlink";
	public static final String ACTION_SPLIT                   = "Split";

	public static final String ACTION_FIND                    = "Find";
	public static final String ACTION_REPLACE                 = "Replace";
	public static final String ACTION_GOTO                    = "GoTo";
	public static final String ACTION_RECALCULATE             = "Recalculate";

	public static final String ACTION_GANTT                   = "Gantt";
	public static final String ACTION_TRACKING_GANTT          = "TrackingGantt";
	public static final String ACTION_TASK_USAGE_DETAIL       = "TaskUsageDetail";
	public static final String ACTION_RESOURCE_USAGE_DETAIL   = "ResourceUsageDetail";
	public static final String ACTION_NETWORK                 = "Network";
	public static final String ACTION_WBS                     = "WBS";
	public static final String ACTION_RBS                     = "RBS";
	public static final String ACTION_REPORT                  = "Report";
	public static final String ACTION_RESOURCES               = "Resources";
	public static final String ACTION_PROJECTS                = "Projects";
	public static final String ACTION_PROJECTS_DIALOG         = "ProjectsDialog";
	public static final String ACTION_HISTOGRAM               = "Histogram";
	public static final String ACTION_CHARTS                  = "Charts";
	public static final String ACTION_TASK_USAGE              = "TaskUsage";
	public static final String ACTION_RESOURCE_USAGE          = "ResourceUsage";
	public static final String ACTION_NO_SUB_WINDOW           = "NoSubWindow";

	public static final String ACTION_ZOOM_IN                 = "ZoomIn";
	public static final String ACTION_ZOOM_OUT                = "ZoomOut";
	public static final String ACTION_SCROLL_TO_TASK          = "ScrollToTask";

	public static final String ACTION_INSERT_TASK             = "InsertTask";
	public static final String ACTION_INSERT_RESOURCE         = "InsertResource";
	public static final String ACTION_INSERT_RECURRING        = "InsertRecurring";
	public static final String ACTION_INSERT_PROJECT          = "InsertProject";
	public static final String ACTION_INSERT_COLUMN           = "InsertColumn";
	public static final String ACTION_INSERT_HYPERLINK        = "InsertHyperlink";
	public static final String ACTION_NEW             	      = ACTION_INSERT_TASK;

	public static final String ACTION_FONT                    = "Font";
	public static final String ACTION_BAR                     = "Bar";
	public static final String ACTION_TIMESCALE               = "Timescale";
	public static final String ACTION_GRIDLINES               = "Gridlines";
	public static final String ACTION_TEXT_STYLES             = "TextStyles";
	public static final String ACTION_BAR_STYLES              = "BarStyles";
	public static final String ACTION_LAYOUT                  = "Layout";

	public static final String ACTION_CHANGE_WORKING_TIME     = "ChangeWorkingTime";
	public static final String ACTION_ASSIGN_RESOURCES        = "AssignResources";
	public static final String ACTION_LEVEL_RESOURCES         = "LevelResources";
	public static final String ACTION_TRACKING                = "Tracking";
	public static final String ACTION_OPTIONS                 = "Options";

	public static final String ACTION_DELEGATE_TASKS          = "DelegateTasks";
	public static final String ACTION_UPDATE_TASKS            = "UpdateTasks";
	public static final String ACTION_UPDATE_PROJECT          = "UpdateProject";
	public static final String ACTION_SAVE_BASELINE           = "SaveBaseline";
	public static final String ACTION_CLEAR_BASELINE          = "ClearBaseline";
	public static final String ACTION_CALENDAR_OPTIONS        = "CalendarOptions";


	public static final String ACTION_SORT                    = "Sort";
	public static final String ACTION_FILTER                  = "Filter";
	public static final String ACTION_GROUP                   = "Group";
	public static final String ACTION_INFORMATION        	  = "Information";
	public static final String ACTION_NOTES              	  = "Notes";
	public static final String ACTION_PROJECT_INFORMATION     = "ProjectInformation";
	public static final String ACTION_TEAM_FILTER		      = "TeamFilter";
	public static final String ACTION_ENTERPRISE_RESOURCES    = "EnterpriseResources";
	public static final String ACTION_DOCUMENTS				  = "Documents";

	public static final String ACTION_INDENT                  = "Indent";
	public static final String ACTION_OUTDENT                 = "Outdent";
	public static final String ACTION_EXPAND                = "Expand";
	public static final String ACTION_COLLAPSE                = "Collapse";
	public static final String ACTION_HIDE_ASSIGNMENTS        = "HideAssignments";
	public static final String ACTION_HIDE_OUTLINE_SYMBOLS    = "HideOutlineSymbols";

	public static final String ACTION_ALL_CHILDREN            = "AllChildren";
	public static final String ACTION_LEVEL1                  = "Level1";
	public static final String ACTION_LEVEL2                  = "Level2";
	public static final String ACTION_LEVEL3                  = "Level3";
	public static final String ACTION_LEVEL4                  = "Level4";
	public static final String ACTION_LEVEL5                  = "Level5";
	public static final String ACTION_LEVEL6                  = "Level6";
	public static final String ACTION_LEVEL7                  = "Level7";
	public static final String ACTION_LEVEL8                  = "Level8";
	public static final String ACTION_LEVEL9                  = "Level9";


	public static final String ACTION_ABOUT_PROJITY           = "AboutProjity";
	public static final String ACTION_PROJITY_DOCUMENTATION   = "ProjityDocumentation";
	public static final String ACTION_TIP_OF_THE_DAY	  	  = "TipOfTheDay";
	public static final String ACTION_OPENPROJ	  	  = "OpenProj";
	public static final String ACTION_PROJECTLIBRE	  	  = "ProjectLibre";


	public static final String ACTION_CHOOSE_FILTER			  = "ChooseFilter";
	public static final String ACTION_CHOOSE_SORT			  = "ChooseSort";
	public static final String ACTION_CHOOSE_GROUP			  = "ChooseGroup";

	public static final String ACTION_PRINTPREVIEW_PRINT			  = "PrintPreviewPrint";
	public static final String ACTION_PRINTPREVIEW_PDF			  = "PrintPreviewPDF";
	public static final String ACTION_PRINTPREVIEW_FORMAT			  = "PrintPreviewFormat";
	public static final String ACTION_PRINTPREVIEW_BACK			  = "PrintPreviewBack";
	public static final String ACTION_PRINTPREVIEW_FORWARD			  = "PrintPreviewForward";
	public static final String ACTION_PRINTPREVIEW_UP			  = "PrintPreviewUp";
	public static final String ACTION_PRINTPREVIEW_DOWN			  = "PrintPreviewDown";
	public static final String ACTION_PRINTPREVIEW_FIRST			  = "PrintPreviewFirst";
	public static final String ACTION_PRINTPREVIEW_LAST			  = "PrintPreviewLast";
	public static final String ACTION_PRINTPREVIEW_ZOOMIN			  = "PrintPreviewZoomIn";
	public static final String ACTION_PRINTPREVIEW_ZOOMOUT			  = "PrintPreviewZoomOut";
	public static final String ACTION_PRINTPREVIEW_ZOOMRESET			  = "PrintPreviewZoomReset";
	public static final String ACTION_PRINTPREVIEW_LEFT_VIEW			  = "PrintPreviewLeftView";
	public static final String ACTION_PRINTPREVIEW_RIGHT_VIEW			  = "PrintPreviewRightView";

	public static final String ACTION_PALETTE                = "Palette";
	public static final String ACTION_LOOK_AND_FEEL                = "LookAndFeel";
	public static final String ACTION_FULL_SCREEN			=	"FullScreen";
	public static final String ACTION_REFRESH			=	"Refresh";

}
