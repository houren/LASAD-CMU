package lasad.gwt.client.ui.workspace.argumentmap;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map;

import lasad.gwt.client.LASAD_Client;
import lasad.gwt.client.communication.LASADActionSender;
import lasad.gwt.client.communication.helper.ActionFactory;
import lasad.gwt.client.helper.connector.Direction;
import lasad.gwt.client.importer.ARGUNAUT.ArgunautParser;
import lasad.gwt.client.importer.LARGO.LARGOParser;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.model.AbstractUnspecifiedElementModel;
import lasad.gwt.client.model.ElementInfo;
import lasad.gwt.client.model.GraphMapInfo;
import lasad.gwt.client.model.argument.MVCViewSession;
import lasad.gwt.client.model.argument.MVController;
import lasad.gwt.client.settings.DebugSettings;
import lasad.gwt.client.ui.box.AbstractBox;
import lasad.gwt.client.ui.link.AbstractLink;
import lasad.gwt.client.ui.workspace.LASADInfo;
import lasad.gwt.client.ui.workspace.argumentmap.elements.CreateSpecialLinkDialogArgument;
import lasad.gwt.client.ui.workspace.graphmap.AbstractGraphMap;
import lasad.gwt.client.ui.workspace.graphmap.GraphMapMenuBar;
import lasad.gwt.client.ui.workspace.graphmap.GraphMapSpace;
import lasad.gwt.client.ui.workspace.graphmap.elements.AbstractCreateSpecialLinkDialog;
import lasad.gwt.client.ui.workspace.loaddialogues.ExportScreenShotDialogue;
import lasad.gwt.client.ui.workspace.tableview.ArgumentEditionStyleEnum;
import lasad.gwt.client.ui.workspace.tableview.TableZoomEnum;
import lasad.gwt.client.ui.workspace.tableview.argument.MapTableArgument;
import lasad.gwt.client.xml.MapToXMLConverter;
import lasad.shared.communication.objects.parameters.ParameterTypes;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.impl.DOMParseException;

import lasad.gwt.client.model.organization.AutoOrganizer;
import lasad.gwt.client.model.organization.ArgumentModel;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.EdgeCoords;
import lasad.gwt.client.ui.workspace.graphmap.elements.DeleteContributionDialog;
import lasad.gwt.client.ui.workspace.graphmap.elements.DeleteRelationDialog;

import lasad.gwt.client.ui.common.elements.AbstractExtendedTextElement;
import lasad.gwt.client.ui.workspace.argumentmap.CreatePreferencesDialog;
import com.extjs.gxt.ui.client.widget.Component;

/**
 *	Finishes the implementation of the map's menu bar.
 *	@author Unknown
 *	@since Unknown, Last Updated 21 July 2015 by Kevin Loughlin
 */
public class ArgumentMapMenuBar extends GraphMapMenuBar {

	private final LASADActionSender communicator = LASADActionSender.getInstance();
	private final ActionFactory actionBuilder = ActionFactory.getInstance();

	public ArgumentMapMenuBar(GraphMapSpace mySpace) {
		super(mySpace);
	}

	@Override
	protected GraphMapInfo getDrawingAreaInformation() {
		MVCViewSession viewSession = (MVCViewSession) myMapSpace.getSession();
		return viewSession.getController().getMapInfo();
		// return myMapSpace.getSession().getController().getMapInfo();
	}

	@Override
	public void removeFeedbackEngine(String agentID, String typeID) {
		if (itemFeedback != null) {
			for (int i = 0; i < itemFeedback.getMenu().getItemCount(); i++) {
				MenuItem x = (MenuItem) itemFeedback.getMenu().getItem(i);
				Logger.log("i = " + i, Logger.DEBUG);
				Logger.log("MENUITEM TEXT: " + x.getText(), Logger.DEBUG);
				Logger.log("AGENTID: " + agentID, Logger.DEBUG);
				Logger.log("TYPEID:" + typeID, Logger.DEBUG);
				if (x.getText().equals(agentID + ": " + typeID)) {
					itemFeedback.getMenu().remove(x);
					break;
				}
			}
		}
	}

	@Override
	public void addFeedbackEngine(final String agentName, final String feedbackName) {
		if (itemFeedback != null) {
			MenuItem newFeedbackAgentItem = new MenuItem(agentName + ": " + feedbackName);
			newFeedbackAgentItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				@Override
				public void componentSelected(MenuEvent me) {
					// Send Action --> Server
					ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().deleteAllFeedbackClusters();
					communicator.sendActionPackage(actionBuilder.requestFeedback(
							ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getID(), LASAD_Client.getInstance().getUsername(),
							agentName, feedbackName));
				}
			});
			itemFeedback.getMenu().add(newFeedbackAgentItem);
		} else
			Logger.log("FEEDBACK MENU IS NULL", Logger.DEBUG_ERRORS);
	}

	@Override
	public void addFeedbackEngine(final String agentName, final String feedbackName, final String agentType) {
		if (itemFeedback != null) {
			MenuItem newFeedbackAgentItem = new MenuItem(agentName + ": " + feedbackName);
			newFeedbackAgentItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				@Override
				public void componentSelected(MenuEvent me) {
					// Send Action --> Server
					ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().deleteAllFeedbackClusters();
					communicator.sendActionPackage(actionBuilder.requestFeedback(
							ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getID(), LASAD_Client.getInstance().getUsername(),
							agentName, feedbackName, agentType));
				}
			});
			itemFeedback.getMenu().add(newFeedbackAgentItem);
		} else
			Logger.log("FEEDBACK MENU IS NULL", Logger.DEBUG_ERRORS);
	}

	@Override
	public void createMenuBar()
	{
		Button itemLASAD = new Button("LASAD");
		Button itemFile = new Button("File");
		Button itemEdit = new Button(myConstants.EditMenu());
		Button itemView = new Button("View");

		Menu lasadMenu = createLASADmenu();
		Menu fileMenu = createFileMenu();
		Menu editMenu = createEditMenu();
		Menu viewMenu = createViewMenu();

		itemLASAD.setMenu(lasadMenu);
		itemFile.setMenu(fileMenu);
		itemEdit.setMenu(editMenu);
		itemView.setMenu(viewMenu);

		this.add(itemLASAD);
		this.add(itemFile);
		this.add(itemEdit);
		this.add(itemView);

		Button itemGroup = new Button(myConstants.GroupTools());
		Menu groupMenu = createGroupMenu();
		itemGroup.setMenu(groupMenu);

		if (groupMenu.getItemCount() > 0)
		{
			this.add(itemGroup);
		}

		if (myMapInfo.isFeedback())
		{
			itemFeedback = new Button("Feedback");
			Menu feedbackMenu = createFeedbackMenu();
			itemFeedback.setMenu(feedbackMenu);
			this.add(itemFeedback);
		}

		if (editionStyle == ArgumentEditionStyleEnum.TABLE)
		{
			createZoomMenu();
		}

		if (LASAD_Client.getInstance().getRole().equalsIgnoreCase("developer"))
		{
			Button itemDebug = new Button("Debug");
			Menu debugMenu = createDebugMenu();
			itemDebug.setMenu(debugMenu);
			this.add(itemDebug);
		}
	}

	// An attempt originally by Kevin Loughlin to downsize screen shot, but that now works thanks to Darlan Sanatana Farias
	// perhaps we should change the method name ;)
	protected MenuItem kevinCreateScreenshotItem() {
		final MenuItem screenshot = new MenuItem("Create a screenshot");
		screenshot.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent me) {
				// extend the size of map to adjust itself to the windows
				((ArgumentMap) myMapSpace.getMyMap()).extendMapDimension(Direction.RIGHT,
						(myMapSpace.getMyMap().getMapDimensionSize().width / myMapSpace.getMyMap().getOffsetWidth() + 1)
								* myMapSpace.getMyMap().getOffsetWidth() - myMapSpace.getMyMap().getMapDimensionSize().width);
				((ArgumentMap) myMapSpace.getMyMap()).extendMapDimension(Direction.DOWN,
						(myMapSpace.getMyMap().getMapDimensionSize().height / myMapSpace.getMyMap().getOffsetHeight() + 1)
								* myMapSpace.getMyMap().getOffsetHeight() - myMapSpace.getMyMap().getMapDimensionSize().height);

				// save position in order to return to this position after screenshot
				final int leftBefore = myMapSpace.getMyMap().getBody().getScrollLeft();
				final int topBefore = myMapSpace.getMyMap().getBody().getScrollTop();

				ExportScreenShotDialogue.getInstance().showLoadingScreen();

				EdgeCoords edgeCoords = ArgumentModel.getInstanceByMapID(myMapInfo.getMapID()).calcEdgeCoords();
				final int TOP = edgeCoords.getTop();
				final int LEFT = edgeCoords.getLeft();
				final int RIGHT = edgeCoords.getRight();
				final int BOTTOM = edgeCoords.getBottom();
				final int WIDTH = RIGHT - LEFT;
				final int HEIGHT = BOTTOM - TOP;
				final int interval_H = myMapSpace.getMyMap().getOffsetHeight();
				final int interval_W = myMapSpace.getMyMap().getOffsetWidth();
				
				// roll map to the beginning
				myMapSpace.getMyMap().getBody().scrollTo("top", TOP);
				myMapSpace.getMyMap().getBody().scrollTo("left", LEFT);

				// make a screen shot
				captureMap(myMapSpace.getMyMap().getBody().getId());

				Timer t = new Timer() {
					// the size of the windows
					int position_H = TOP;
					int position_W = LEFT;
					int sum = 0;
					boolean isFinished = false;
					int numberOfColumns = (int) Math.ceil(WIDTH * 1.0 / interval_W);
					int numberOfRows = (int) Math.ceil(HEIGHT * 1.0 / interval_H);
					int numOfAllImages = numberOfColumns*numberOfRows;
					int i = 1; int j = 1;
					
					public void run() {
						
						if (!isFinished) {
							RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getModuleBaseURL()
									+ "ScreenShot");
							RequestCallback handler = new RequestCallback() {

								@Override
								public void onError(Request request, Throwable e) {
									if (DebugSettings.debug_errors)
										Logger.log(e.toString(), Logger.DEBUG_ERRORS);
								}

								@Override
								public void onResponseReceived(Request request, Response response) {
									// Browser will open a save file dialog box
									// System.err.println("ScreenShot sendet!");
								}
							};

							// update the step of the process
							ExportScreenShotDialogue.getInstance().updateProgress(((float) sum ) / numOfAllImages);

							try {
								// get the screenshot and send it to the servlet
								String map_temp = showMap();
								map_temp = map_temp.substring(map_temp.indexOf(",") + 1);
								builder.sendRequest(LASAD_Client.getInstance().getUsername() + "_" + myMapInfo.getMapID() + sum
										+ ":" + map_temp, handler);
							} catch (RequestException e) {
								e.printStackTrace();
							}
							// if not end, roll the map to next window
							if (i < numberOfRows+1) {
								if (j < numberOfColumns) {
									position_W += interval_W;
									j++;
								} else {
									position_H += interval_H;
									position_W = LEFT;
									i++;
									j = 1;
								}

								myMapSpace.getMyMap().getBody().scrollTo("top", position_H);
								myMapSpace.getMyMap().getBody().scrollTo("left", position_W);
								captureMap(myMapSpace.getMyMap().getBody().getId());
								sum++;
							} else {
								ExportScreenShotDialogue.getInstance().updateProgress((float) 1);
								// return to previous position
								myMapSpace.getMyMap().getBody().scrollTo("top", topBefore);
								myMapSpace.getMyMap().getBody().scrollTo("left", leftBefore);
								isFinished = true;
								RequestBuilder builder_end = new RequestBuilder(RequestBuilder.POST, GWT.getModuleBaseURL()
										+ "ScreenShotMerge");
								try {
									// calculate the cols and rows of the image
									int cols = numberOfColumns;
									int rows = numberOfRows;
									String format = LASAD_Client.getInstance().getUsername() + "_" + myMapInfo.getMapID() + ","
											+ rows + ":" + cols;

									Logger.log(format, Logger.DEBUG);

									builder_end.sendRequest(format, new RequestCallback() {
										@Override
										public void onError(Request request, Throwable e) {
											if (DebugSettings.debug_errors)
												Logger.log(e.toString(), Logger.DEBUG_ERRORS);
										}

										@Override
										public void onResponseReceived(Request request, Response response) {
											// Browser will open a save file dialog box
											ExportScreenShotDialogue.getInstance().closeLoadingScreen();
											com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "ScreenShotMerge",
													"_blank", "enabled");
											// System.err.println("Screenshot mergen!");
										}
									});
									this.cancel();
								} catch (RequestException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					}
				};

				// delay running for 3 seconds
				t.scheduleRepeating(3000);
			}
		});

		return screenshot;
	}
	
	// TODO Zhenyu, currently unused
	private MenuItem createSearchBox() {
		final MenuItem searchbox = new MenuItem("search the Text of Boxes.....");
		searchbox.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				// TODO Auto-generated method stub
				final Window w = new Window();
				w.setHeading("Searching Boxes");
				FormData formData = new FormData("-20");

				com.extjs.gxt.ui.client.widget.form.FormPanel simple = new com.extjs.gxt.ui.client.widget.form.FormPanel();
				simple.setBorders(false);
				simple.setBodyBorder(false);
				simple.setLabelWidth(55);
				simple.setPadding(5);
				simple.setHeaderVisible(false);

				final TextField<String> keyword = new TextField<String>();
				keyword.setFieldLabel("Keywords");
				keyword.setEmptyText("Please input the keywords....");
				keyword.setAllowBlank(false);
				simple.add(keyword, formData);

				final SimpleComboBox<String> combo = new SimpleComboBox<String>();
				combo.setFieldLabel("where");
				combo.add("all maps");
				combo.add("this map");
				combo.setSimpleValue("all maps");
				simple.add(combo, formData);

				Button b = new Button("Submit");
				b.addSelectionListener(new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						if (keyword.getValue() != null) {
							// communicator.sendActionPackage(actionBuilder.searchForBoxes(keyword.getValue(),combo.getSelectedIndex()+"",""+myMapInfo.getMapID()));
							w.setVisible(false);
						}
					}
				});
				simple.addButton(b);

				w.add(simple);
				w.show();

			}

		});

		return searchbox;
	}

	// TODO Zhenyu
	public static native void captureMap(String id) /*-{
		$wnd.captureScreenShot(id);
	}-*/;

	// TODO Zhenyu
	public static native String showMap() /*-{
		return $wnd.getImage();
	}-*/;

	@Override
	protected void handleCreateNewBoxItemSelectionEvent(MenuEvent me, final ElementInfo currentElement) {
		int tempPosX = ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getHScrollPosition()
				+ ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getInnerWidth() / 2
				- (Integer.parseInt(currentElement.getUiOption(ParameterTypes.Width)) / 2);
		int tempPosY = ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getVScrollPosition()
				+ ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getInnerHeight() / 2
				- (Integer.parseInt(currentElement.getUiOption(ParameterTypes.Height)) / 2);
		ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
		communicator.sendActionPackage(actionBuilder.createBoxWithElements(currentElement, ArgumentMapMenuBar.this.getMyMapSpace()
				.getMyMap().getID(), tempPosX, tempPosY));
	}

	@Override
	protected AbstractCreateSpecialLinkDialog createSpecialLinkDialog(ElementInfo config, String mapId, TreeMap<String, AbstractBox> boxes,
			TreeMap<String, AbstractLink> links) {
		return new CreateSpecialLinkDialogArgument(config, mapId, boxes, links);
	}

	@Override
	protected void handleGroupPointerItemSelectionEvent(MenuEvent me, int tempPosX, int tempPosY) {
		communicator.sendActionPackage(actionBuilder.createGroupCursor(ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getID(),
				LASAD_Client.getInstance().getUsername(), tempPosX, tempPosY));
	}

	private Menu createGroupMenu() {
		Menu groupMenu = new Menu();

		if (myMapInfo.isGroupPointer()) {
			MenuItem groupPointerItem = createGroupPointerItem();
			groupMenu.add(groupPointerItem);
		}
		return groupMenu;
	}

	private Menu createDebugMenu() {
		Menu debugMenu = new Menu();

		// Creates the sub menu for box types
		MenuItem setDebugSettings = createSetDebugSettingsItem();
		debugMenu.add(setDebugSettings);

		MenuItem editSpots = createEditAttributeItem();
		debugMenu.add(editSpots);

		MenuItem getBoxPairsOfDifferentUsers = getBoxPairsOfDifferentUsersItem();
		debugMenu.add(getBoxPairsOfDifferentUsers);

		return debugMenu;
	}

	private Menu createFeedbackMenu() {
		Menu menu = new Menu();

		MenuItem deleteFeedback = deleteFeedbackItem();
		menu.add(deleteFeedback);

		return menu;
	}

	private void createZoomMenu() {
		Button zoom = new Button("Zoom");
		Menu zoomMenu = new Menu();

		for (final TableZoomEnum e : TableZoomEnum.values()) {
			MenuItem item = new MenuItem(e.toString());
			zoomMenu.add(item);

			item.addSelectionListener(new SelectionListener<MenuEvent>() {

				@Override
				public void componentSelected(MenuEvent ce) {

					AbstractGraphMap argumentMap = myMapSpace.getMyMap();
					if (argumentMap instanceof MapTableArgument) {

						MapTableArgument argumentMapTable = (MapTableArgument) argumentMap;

						argumentMapTable.resize(e);

					}
				}
			});
		}

		zoom.setMenu(zoomMenu);
		add(zoom);
	}

	private MenuItem createEditAttributeItem() {
		final MenuItem editAttributeItem = new MenuItem("Edit Spots");
		editAttributeItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent me) {
				editAttributeItem.getParentMenu().hide();
				final MessageBox box = MessageBox.prompt("Edit Attribute", "Code-Pattern: Element-ID,Attribute,Value");
				box.addCallback(new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent be) {
						if (be.getButtonClicked().getText().equalsIgnoreCase("OK")) {
							if (be.getValue() == null) {
								LASADInfo.display("Error", "No valid value entered.");
							} else {
								String[] input = be.getValue().split(",");
								String id = input[0];
								String attribute = input[1];
								String value = "";
								if (input.length == 3) {
									value = input[2];
								}
								// communicator.sendActionPackage(actionBuilder.editAttribute(GraphMapMenuBar.this.getMyMapSpace().getMyMap().getID(),
								// id, attribute, value));
							}
						}
					}
				});
			}
		});
		return editAttributeItem;
	}

	private MenuItem getBoxPairsOfDifferentUsersItem() {
		final MenuItem editAttributeItem = new MenuItem("Count number of connected box pairs with different authors");
		editAttributeItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent me) {

				MVController myController = LASAD_Client.getMVCController(myMapSpace.getMyMap().getID());

				int number = 0;
				// If object is a box, protect all links that are connected to
				// the box as well as all boxes that are connected to these
				// links
				Iterator<AbstractUnspecifiedElementModel> myProtectionIterator = myController.getAllElements().iterator();
				while (myProtectionIterator.hasNext()) {
					AbstractUnspecifiedElementModel item = myProtectionIterator.next();
					if (item.getParents().size() >= 2) {
						if (!item.getParents().get(0).getAuthor().equalsIgnoreCase(item.getParents().get(1).getAuthor())) {
							number++;
						}
					}
				}

				LASADInfo.display("Result", number + "");

			}
		});

		return editAttributeItem;
	}

	private MenuItem deleteFeedbackItem() {
		final MenuItem deleteFeedbackItem = new MenuItem(myConstants.DeleteAllFeedbackMenuItem());
		deleteFeedbackItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent me) {
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().deleteAllFeedbackClusters();
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
			}
		});
		return deleteFeedbackItem;
	}

	/*
	 *	Create the menu for logging out
	 */
	protected Menu createLASADmenu()
	{
		Menu menu = new Menu();

		/*
		MenuItem preferencesItem = createPreferencesItem();
		menu.add(preferencesItem);
		*/

		MenuItem logOut = createLogOutItem();
		menu.add(logOut);

		return menu;
	}

	/*
	 *	Create menu for file operations (closing, exporting, etc)
	 */
	protected Menu createFileMenu()
	{
		Menu menu = new Menu();

		MenuItem exportItem = createExportItem();
		menu.add(exportItem);

		MenuItem screenshot = kevinCreateScreenshotItem();
		menu.add(screenshot);

		MenuItem closeMap = createCloseMapItem();
		menu.add(closeMap);

		return menu;
	}

	/*
	 *	Create the menu for editing (add, delete, autoOrganize, maybe undo if we implement that, etc.)
	 */
	protected Menu createEditMenu() {
		Menu menu = new Menu();

		// TODO Re-implement the undo function
		// MenuItem editUndoItem = createEditUndoItem();
		// menu.add(editUndoItem);

		MenuItem addItem = createAddItem();
		menu.add(addItem);

		MenuItem deleteItem = createDeleteItem();
		menu.add(deleteItem);

		MenuItem fontSizeItem = createFontSizeItem();
		menu.add(fontSizeItem);

		MenuItem autoOrganizeItem = createAutoOrganizeItem();
		menu.add(autoOrganizeItem);

		return menu;
	}

	/*
	 *	Create the menu for viewing (all there is right now is finding a contribution)
	 */
	protected Menu createViewMenu()
	{
		Menu menu = new Menu();
		MenuItem findContribution = createFindContributionItem();
		menu.add(findContribution);
		return menu;
	}

	/*
	protected MenuItem createPreferencesItem()
	{
		final MenuItem preferences = new MenuItem("Preferences");
		preferences.addSelectionListener(new SelectionListener<MenuEvent>()
		{
			@Override
			public void componentSelected(MenuEvent me)
			{
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				CreatePreferencesDialog preferencesDialog = new CreatePreferencesDialog(ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getID());
				preferencesDialog.show();
			}
		});
		return preferences;
	}
	*/

	/*
	 *	Create the subitem of the LASAD menu that allows a user to logout when clicked
	 */
	protected MenuItem createLogOutItem()
	{
		final MenuItem logOutItem = new MenuItem("Logout");
		logOutItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent me)
			{
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				LASAD_Client.getInstance().logOut();
			}
		});
		return logOutItem;
	}

	/*
	 *	Subitem of the file menu that allows the user to export the map
	 */
	protected MenuItem createExportItem()
	{
		final MenuItem saveItem = new MenuItem("Export map");
		saveItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				MapToXMLConverter conv = new MapToXMLConverter(ArgumentMapMenuBar.this.getMyMapSpace().getMyMap(),
						ArgumentMapMenuBar.this.myMapInfo.getXmlOntology(), ArgumentMapMenuBar.this.myMapInfo.getXmlTemplate());

				// Send the xml-string as a post request to servlet
				// Get the file name via http-response, then open file location
				RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getModuleBaseURL() + "saveToXmlServlet");
				RequestCallback handler = new RequestCallback() {

					@Override
					public void onError(Request request, Throwable e) {
						if (DebugSettings.debug_errors)
							Logger.log(e.toString(), Logger.DEBUG_ERRORS);
					}

					@Override
					public void onResponseReceived(Request request, Response response) {
						// Browser will open a save file dialog box
						com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "saveToXmlServlet", "_blank", "enabled");
					}
				};
				try {
					builder.sendRequest(conv.getXmlString(), handler);
				} catch (RequestException e) {
					e.printStackTrace();
				}
			}
		});
		return saveItem;
	}

	/*
	 *	Subitem of the file menu that lets the user close the current map and return to the map overview screen when clicked
	 */
	protected MenuItem createCloseMapItem()
	{
		final MenuItem closeItem = new MenuItem("Close");
		closeItem.addSelectionListener(new SelectionListener<MenuEvent>()
		{
			@Override
			public void componentSelected(MenuEvent ce)
			{
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				communicator.sendActionPackage(actionBuilder.leaveMap(ArgumentMapMenuBar.this.getMyMapInfo().getMapID()));
			}
		});
		return closeItem;
	}

	/*
	 *	Subitem of the edit menu that allows an alternative way to click and drag for adding an item to the map
	 */
	protected MenuItem createAddItem()
	{
		MenuItem addItem = new MenuItem("Add");
		addItem.setSubMenu(createBoxLinkMenu(true));
		return addItem;
	}

	/*
	 *	Subitem of the edit menu that allows an alternative way to clicking for deleting an item from the map
	 */
	protected MenuItem createDeleteItem()
	{
		MenuItem deleteItem = new MenuItem("Delete");
		deleteItem.setSubMenu(createBoxLinkMenu(false));
		return deleteItem;
	}

	/*
	 *	Provides "Contribution" and "relation" as options
	 *	@param useSubList - If true, (the case of add), provides the types of contributions relations as subitems.
	 *	If false, make contribution and relation respond to clicks to create a dialog to delete items
	 */
	protected Menu createBoxLinkMenu(final boolean useSubList)
	{
		Menu boxLinkMenu = new Menu();
		boxLinkMenu.add(createContributionItem(useSubList));
		boxLinkMenu.add(createRelationItem(useSubList));
		return boxLinkMenu;
	}

	protected MenuItem createFontSizeItem()
	{
		MenuItem fontSizeItem = new MenuItem("Select font size");
		fontSizeItem.setSubMenu(createSizeSelector());
		return fontSizeItem;
	}

	protected Menu createSizeSelector()
	{
		Menu sizeOptions = new Menu();
		for (int i = 8; i < 37; i += 2)
		{
			sizeOptions.add(createNextFontSize(i));
		}
		return sizeOptions;
	}

	protected CheckMenuItem createNextFontSize(final int i)
	{
		final CheckMenuItem fontSize = new CheckMenuItem(String.valueOf(i));
		if (i == ArgumentModel.getInstanceByMapID(ArgumentMapMenuBar.this.getMyMapInfo().getMapID()).getFontSize())
		{
			fontSize.setChecked(true);
		}
		fontSize.addSelectionListener(new SelectionListener<MenuEvent>()
		{
			@Override
			public void componentSelected(MenuEvent me)
			{
				for (Component menuComponent : fontSize.getParentMenu().getItems())
				{
					if (menuComponent instanceof CheckMenuItem)
					{
						((CheckMenuItem) menuComponent).setChecked(false);
					}
				}

				fontSize.setChecked(true);
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				ArgumentModel.getInstanceByMapID(ArgumentMapMenuBar.this.getMyMapInfo().getMapID()).setFontSize(i);
			}
		});
		return fontSize;
	}

	/*
	 *	Create the item listed as contribution for the add and delete subitems.
	 *	@param useSubList - if true, add the types of contributions as subitems.  If false, make the contribution button clickable.
	 */
	protected MenuItem createContributionItem(final boolean useSubList)
	{
		final MenuItem boxMenu = new MenuItem(myConstants.ContributionMenuItem());
		if (useSubList)
		{
			Menu subBoxes = new Menu();

			// Collect box types
			
			Map<String, ElementInfo> boxes = myMapInfo.getElementsByType("box");
			if(boxes != null) {
				for (ElementInfo info : boxes.values()) {
					subBoxes.add(createNewBoxItem(info));
				}
			}

			boxMenu.setSubMenu(subBoxes);
		}
		else
		{
			boxMenu.addSelectionListener(new SelectionListener<MenuEvent>()
			{
				@Override
				public void componentSelected(MenuEvent me)
				{
					ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
					DeleteContributionDialog boxDialog = new DeleteContributionDialog(getMyMapSpace().getMyMap().getID());
					boxDialog.show();
				}
			});
		}
		return boxMenu;
	}

	/*
	 *	Create the item listed as relation for the add and delete subitems.
	 *	@param useSubList - if true, add the types of relations as subitems.  If false, make the relation button clickable.
	 */
	protected MenuItem createRelationItem(final boolean useSubList)
	{
		// Creates the sub menu for link types
		final MenuItem linkMenu = new MenuItem(myConstants.RelationMenuItem());
		if (useSubList)
		{
			Menu subLinks = new Menu();

			// Collect link types
			Map<String, ElementInfo> relations = myMapInfo.getElementsByType("relation");
			if(relations != null) {
				for (ElementInfo info : relations.values()) {
					subLinks.add(createNewLinkItem(info));
				}
			}
			
			linkMenu.setSubMenu(subLinks);
		}
		else
		{
			linkMenu.addSelectionListener(new SelectionListener<MenuEvent>()
			{
				@Override
				public void componentSelected(MenuEvent me)
				{
					ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
					DeleteRelationDialog linkDialog = new DeleteRelationDialog(getMyMapSpace().getMyMap().getID());
					linkDialog.show();
				}
			});
		}
		return linkMenu;
	}

	/*
	 *	Subitem of the edit menu that autoOrganizes the map when subitem (specifying orientation) is pressed
	 */
	protected MenuItem createAutoOrganizeItem()
	{
		final MenuItem autoOrganizeItem = new MenuItem("Auto Organization");
		autoOrganizeItem.setSubMenu(chooseOrganizationStyle());
		return autoOrganizeItem;
	}

	protected Menu chooseOrganizationStyle()
	{
		Menu runOrAdjust = new Menu();
		runOrAdjust.add(createRunOrganizer());
		runOrAdjust.add(createOrganizerSettings());
		return runOrAdjust;
	}

	protected MenuItem createRunOrganizer()
	{
		final MenuItem runOrganizer = new MenuItem("Organize Map");
		runOrganizer.addSelectionListener(new SelectionListener<MenuEvent>()
		{
			@Override
			public void componentSelected(MenuEvent ce)
			{
				AutoOrganizer myOrganizer = ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getAutoOrganizer();
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				myOrganizer.organizeMap();
			}
		});
		return runOrganizer;
	}

	protected MenuItem createOrganizerSettings()
	{
		final MenuItem organizerPreferences = new MenuItem("Preferences");
		organizerPreferences.addSelectionListener(new SelectionListener<MenuEvent>()
		{
			@Override
			public void componentSelected(MenuEvent ce)
			{
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				CreatePreferencesDialog preferencesDialog = new CreatePreferencesDialog(ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getID());
				preferencesDialog.show();
			}
		});
		return organizerPreferences;
	}
}