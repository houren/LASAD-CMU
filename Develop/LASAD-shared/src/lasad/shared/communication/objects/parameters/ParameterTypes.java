package lasad.shared.communication.objects.parameters;

public enum ParameterTypes {
	UserName("USERNAME"), Id("ID"), MapId("MAP-ID"), Received("RECEIVED"), Time("TIME"), Type("TYPE"), Parent("PARENT"), Persistent(
			"PERSISTENT"), ElementId("ELEMENT-ID"), Replay("REPLAY"), ReplayTime("REPLAY-TIME"), OntologyName("ONTOLOGYNAME"), Ontology_Name(
			"ONTOLOGY-NAME"), OntologyXML("ONTOLOGY-XML"), TemplateName("TEMPLATE-NAME"), TemplateDescription("TEMPLATE-DESCRIPTION"), MaxUsers(
			"MAX-USERS"), UseChat("USE-CHAT"), UseUserlist("USE-USERLIST"), UseCursorTracking("USE-CURSOR-TRACKING"), UseSelectionDetails(
			"USE-SELECTION-DETAILS"), Transcript("TRANSCRIPT"), Password("PW"), Role("ROLE"), Status("STATUS"), Message("MESSAGE"), MapName(
			"MAPNAME"), CreatorId("CREATOR-ID"), CreatorName("CREATORNAME"), TemplateTitle("TEMPLATETITLE"), Created("CREATED"), Modified(
			"MODIFIED"), ActiveUsers("ACTIVEUSERS"), Ontology("ONTOLOGY"), Template("TEMPLATE"), TotalSec("TOTAL-SEC"), TemplateXML(
			"TEMPLATEXML"), TemplateMaxUsers("TEMPLATEMAXUSER"), MapMaxId("MAP-MAXID"), SessionId("SESSION-ID"), TemplateId("TEMPLATE-ID"), TemplateMaxId(
			"TEMPLATE-MAXID"), UserKickOut("USER-KICKOUT"), UserId("USER-ID"), UserMaxId("USER-MAXID"), RestrictedTo("RESTRICTED-TO"), ImportType(
			"IMPORT-TYPE"), OriginalCommand("ORIGINAL-COMMAND"), Session("SESSION"), ContentXML("CONTENT-XML"), ChatLog("CHATLOG"), Opener(
			"OPENER"), TextColor("TEXTCOLOR"), RootElementId("ROOTELEMENTID"), PosX("POS-X"), PosY("POS-Y"), Height("HEIGHT"), Width(
			"WIDTH"), Clone("CLONE"), Confirmed("CONFIRMED"), GetRoles("GET-ROLES"), XMLText("XMLTEXT"), Method("METHOD"), Summary(
			"SUMMARY"), Direction("DIRECTION"), UserActionId("USERACTION-ID"), NumActions("NUM-ACTIONS"), ToRev("TO-REV"), ForUser(
			"FORUSER"), HighlightElementId("HIGHLIGHT-ELEMENT-ID"), Text("TEXT"), AgentType("AGENT-TYPE"), AgentId("AGENT-ID"), AgentName(
			"AGENT-NAME"), TypeId("TYPE-ID"), QuestionId("QUESTIONID"), QuestionAnswer("QUESTIONANSWER"), XMLSaveFile("XML-SAVE-FILE"), Percent(
			"PERCENT"), BoxId("BOX-ID"), StartRow("STARTROW"), StartPoint("STARTPOINT"), EndRow("ENDROW"), EndPoint("ENDPOINT"), ObjectId(
			"OBJECTID"), Score("SCORE"), CreationDate("CREATIONDATE"), ModificationDate("MODIFICATIONDATE"), FirstModificationDate(
			"FIRSTMODIFICATIONDATE"), Faded("FADED"), Highlighted("HIGHLIGHTED"), Highlight("HIGHLIGHT"), MinHeight("minheight"), MaxHeight(
			"maxheight"), MinWidth("minwidth"), MaxWidth("maxwidth"), WindowHeight("windowheight"), WindowWidth("windowwidth"), DefaultURL(
			"defaulturl"), Editable("editable"), TextType("texttype"), Label("label"), LongLabel("longlabel"), ManualAdd("manualadd"), MinScore(
			"minscore"), MaxScore("maxscore"), Options("options"), SelectedOption("selectedoption"), Heading("heading"), BackgroundColor(
			"background-color"), Border("border"), Endings("endings"), LineColor("linecolor"), LineWidth("linewidth"), Details("details"), Selection(
			"SELECTION"), LastId("LAST-ID"), UserJoin("USERJOIN"), Changed("Changed"), Successful("Successful"), OldStatus("OldStatus"), NewStatus(
			"NewStatus"), ChangedComponentID("ChangedComponentID"), challengeId("CHALLENGE_ID"), challengeName("CHALLENGE_NAME"), token(
			"TOKEN"), groupId("GROUP_ID"), passwordEncrypted("passwordEncrypted"), BackgroundImageURL("BackgroundImageURL"), PatternId(
			"PATTERN-ID"), RequestId("REQUEST-ID"), ServiceClass("SERVICE-CLASS"), OrganizeTopToBottom("ORGANIZE_TOP_TO_BOTTOM"), 
			AllowLinksToLinks("ALLOW_LINKS_TO_LINKS"), CanBeGrouped("canbegrouped"), ConnectsGroup("connectsgroup"), LinksAlreadyRemoved("linksalreadyremoved"),
			SiblingsAlreadyUpdated("siblingsalreadyupdated"),

	TextOnImage("textonimage"), Source("source"), Dragable("dragable"), Text1("TEXT1"), Text2("TEXT2"), Spot1("SPOT1"), Spot2("SPOT2"), DetailsOnly(
			"detailsonly"), FontColor("font-color"), Client("CLIENT"), UserList("USERLIST"), Headline("headline"), Link("LINK"), ImageURL(
			"IMAGEURL"), ImageAdjustedWidth("IMAGE_ADJUSTED_WIDTH"), ImageAdjustedHeight("IMAGE_ADJUSTED_Height"), SmallImageHeight(
			"SMALL_IMAGE_HEIGHT"), SmallImageWidth("SMALL_IMAGE_WIDTH"), MyCheck("MYCHECK"), Color("COLOR"), JoinBox("JOINBOX"), ResponseRequired(
			"RESPONSEREQUIRED"), Resizable("RESIZABLE"), MinQuantity("minquantity"), MaxQuantity("maxquantity"), ConfigButton(
			"configbutton"), Reason("Reason"), Succeeded("Succeeded"), OnlyAuthorCanModify("onlyauthorcanmodify"), CommitTextByEnter(
			"committextbyenter"), StraightLink("straightlink"), UseMiniMap("USE-MINIMAP"), ServerVersion("serverVersion"),
	// MODIFIED BY BM
	AutoGrowTextArea("USE-AUTOGROW");
	// MODIFIED BY BM END

	private String oldParameter;

	public String getOldParameter() {
		return oldParameter;
	}

	ParameterTypes(String oldString) {
		oldParameter = oldString;
	}

	public static ParameterTypes fromOldString(String s) {
		for (ParameterTypes p : ParameterTypes.values()) {
			if (s.equalsIgnoreCase(p.getOldParameter())) {
				return p;
			}
		}
		System.err.println("ParameterTypes.fromString: ParameterType " + s + " unknown");
		return null;
		// return valueOf(s);
	}

	public static ParameterTypes fromString(String s) {
		for (ParameterTypes p : ParameterTypes.values()) {
			if (s.equalsIgnoreCase(p.getOldParameter())) {
				return p;
			}
		}
		return valueOf(s);
	}

}
