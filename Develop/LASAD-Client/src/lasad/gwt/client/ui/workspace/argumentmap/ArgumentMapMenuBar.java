package lasad.gwt.client.ui.workspace.argumentmap;

import java.util.Iterator;
import java.util.TreeMap;

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

// Added by Kevin Loughlin for auto organize
import lasad.gwt.client.communication.AutoOrganizer;

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
	public void createMenuBar() {

		Button itemEdit = new Button(myConstants.EditMenu());
		Button itemAdd = new Button(myConstants.AddMenu());
		Button itemGroup = new Button(myConstants.GroupTools());

		Menu editMenu = createEditMenu();
		Menu addMenu = createAddMenu();
		Menu groupMenu = createGroupMenu();
		Menu debugMenu = createDebugMenu();

		itemEdit.setMenu(editMenu);
		itemAdd.setMenu(addMenu);
		itemGroup.setMenu(groupMenu);

		this.add(itemEdit);
		this.add(itemAdd);

		if (groupMenu.getItemCount() > 0) {
			this.add(itemGroup);
		}

		// TODO Zhenyu Geng
		Button itemExtraFunctions = new Button("Extras...");
		Menu extraFunctionsMenu = createExtraFunctions();
		itemExtraFunctions.setMenu(extraFunctionsMenu);
		this.add(itemExtraFunctions);

		if (myMapInfo.isFeedback()) {
			itemFeedback = new Button("Feedback");
			Menu feedbackMenu = createFeedbackMenu();
			itemFeedback.setMenu(feedbackMenu);
			this.add(itemFeedback);
		}

		// Outsourced to Map Login Screen
		// if (myMapInfo.getOntologyName().equalsIgnoreCase("argunaut") || myMapInfo.getOntologyName().equalsIgnoreCase("largo"))
		// {
		// Button itemParse = new Button("Load Map");
		// Menu parseMenu = createParseMenu();
		// itemParse.setMenu(parseMenu);
		// this.add(itemParse);
		// }

		if (editionStyle == ArgumentEditionStyleEnum.TABLE) {
			createZoomMenu();
		}

		if (LASAD_Client.getInstance().getRole().equalsIgnoreCase("developer")) {
			Button itemDebug = new Button("Debug");
			itemDebug.setMenu(debugMenu);
			this.add(itemDebug);
		}
	}

	// TODO Zhenyu
	private Menu createExtraFunctions() {
		Menu extramenu = new Menu();
		MenuItem screenshot = screenshotAction();
		extramenu.add(screenshot);

		// Added by Kevin Loughlin
		//if (myMapInfo.isAutoOrganize() )
		//{
			MenuItem autoOrganizeItem = autoOrganizeAction();
			extramenu.add(autoOrganizeItem);
		//}

		// MenuItem rearchBox = createRearchBox();
		// extramenu.add(rearchBox);

		return extramenu;

	}

	// TODO Zhenyu
	private MenuItem createRearchBox() {
		final MenuItem rearchbox = new MenuItem("search the Text of Boxes.....");
		rearchbox.addSelectionListener(new SelectionListener<MenuEvent>() {

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

		return rearchbox;
	}

	// TODO Zhenyu
	public static native void captureMap(String id) /*-{
		$wnd.captureScreenShot(id);
	}-*/;

	// TODO Zhenyu
	public static native String showMap() /*-{
		return $wnd.getImage();
	}-*/;

	// TODO Zhenyu
	private MenuItem screenshotAction() {
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

				// show the time of this process
				MessageBox box = new MessageBox();
				box.setButtons(MessageBox.YESNO);
				box.setIcon(MessageBox.QUESTION);
				// box.setTitle(myConstants.CloseMapHeader());
				box.setMessage("This Process will last " +
				// 3
				// * (myMapSpace.getMyMap().getMapDimensionSize().width * myMapSpace.getMyMap().getMapDimensionSize().height)
				// / (60 * myMapSpace.getMyMap().getOffsetHeight() * myMapSpace.getMyMap().getOffsetWidth())
				// +
						"several minutes, do you want to continue? ");
				box.addCallback(new Listener<MessageBoxEvent>() {

					public void handleEvent(MessageBoxEvent be) {
						if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
							// save position in order to return to this position after screenshot
							final int leftBefore = myMapSpace.getMyMap().getBody().getScrollLeft();
							final int topBefore = myMapSpace.getMyMap().getBody().getScrollTop();

							ExportScreenShotDialogue.getInstance().showLoadingScreen();
							// roll map to the beginning
							myMapSpace.getMyMap().getBody().scrollTo("top", 0);
							myMapSpace.getMyMap().getBody().scrollTo("left", 0);

							// make a screen shot
							captureMap(myMapSpace.getMyMap().getBody().getId());

							Timer t = new Timer() {
								// the size of the windows
								int interval_H = myMapSpace.getMyMap().getOffsetHeight();
								int interval_W = myMapSpace.getMyMap().getOffsetWidth();
								int position_H = 0;
								int position_W = 0;
								int sum = 0;
								boolean isFinished = false;
								int numOfAllImages = myMapSpace.getMyMap().getMapDimensionSize().width
										* myMapSpace.getMyMap().getMapDimensionSize().height
										/ (myMapSpace.getMyMap().getOffsetHeight() * myMapSpace.getMyMap().getOffsetWidth());

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
										ExportScreenShotDialogue.getInstance().updateProgress((float) sum / numOfAllImages);

										try {
											// get the screenshot and send it to the servlet
											String map_temp = showMap();
											map_temp = map_temp.substring(map_temp.indexOf(",") + 1);
											builder.sendRequest(LASAD_Client.getInstance().getUsername() + "_" + myMapInfo.getMapID() + sum
													+ ":" + map_temp, handler);
										} catch (RequestException e) {
											e.printStackTrace();
										}
										// if not end roll the map to next window
										if (position_H < myMapSpace.getMyMap().getMapDimensionSize().height - interval_H + 1) {
											if (position_W < myMapSpace.getMyMap().getMapDimensionSize().width - interval_W) {
												position_W += interval_W;
											} else {
												position_H += interval_H;
												position_W = 0;
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
												int cols = myMapSpace.getMyMap().getMapDimensionSize().width / interval_W;
												int rows = myMapSpace.getMyMap().getMapDimensionSize().height / interval_H;
												String format = LASAD_Client.getInstance().getUsername() + "_" + myMapInfo.getMapID() + ","
														+ rows + ":" + cols;

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
					}

				});
				box.show();
			}
		});

		return screenshot;

	}

	// Added By Kevin Loughlin for autoOrganize Menu functionality
	private MenuItem autoOrganizeAction()
	{
		final MenuItem autoOrganizeItem = new MenuItem("Auto organize this map");

		autoOrganizeItem.addSelectionListener(new SelectionListener<MenuEvent>()
		{
			@Override
			public void componentSelected(MenuEvent ce)
			{
				Logger.log("[lasad.gwt.client.ui.workspace.argumentmap.ArgumentMapMenuBar][autoOrganizeAction] Starting autoOrganize...", Logger.DEBUG);
				AutoOrganizer autoOrganizer = new AutoOrganizer(ArgumentMapMenuBar.this.getMyMapSpace().getMyMap() );
				autoOrganizer.organizeMap();
				Logger.log("[lasad.gwt.client.ui.workspace.argumentmap.ArgumentMapMenuBar][autoOrganizeAction] Completed autoOrganize...", Logger.DEBUG);
			}
		});

		return autoOrganizeItem;
	}

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

	private Menu createEditMenu() {
		Menu menu = new Menu();
		// TODO Re-implement the undo function
		// MenuItem editUndoItem = createEditUndoItem();
		// menu.add(editUndoItem);
		MenuItem findContribution = createFindContributionItem();
		menu.add(findContribution);

		MenuItem saveItem = createSaveItem();
		menu.add(saveItem);

		// MenuItem loadItem = createLoadItem();
		// menu.add(loadItem);

		MenuItem logOutItem = createLogOutItem();
		menu.add(logOutItem);

		return menu;
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

	private MenuItem createSaveItem() {
		final MenuItem saveItem = new MenuItem("Export map...");
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

	private MenuItem createLogOutItem() {
		final MenuItem logOutItem = new MenuItem("Logout");
		logOutItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent me) {
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				logOutItem.getParentMenu().hide();
				LASAD_Client.getInstance().logOut();
			}
		});
		return logOutItem;
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

	private MenuItem createLoadItem() {
		final MenuItem loadItem = new MenuItem("Load Map");
		loadItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				// The dialog window
				final Window window = new Window();

				// Create a FormPanel and point it at a service.
				final FormPanel form = new FormPanel();
				form.setAction(GWT.getModuleBaseURL() + "fileupload");

				// set the form to use the POST method, and multipart MIME
				// encoding for file upload
				form.setEncoding(FormPanel.ENCODING_MULTIPART);
				form.setMethod(FormPanel.METHOD_POST);

				// Create a panel to hold all of the form widgets.
				VerticalPanel panel = new VerticalPanel();
				form.setWidget(panel);

				// Create a FileUpload widget.
				final FileUpload upload = new FileUpload();
				upload.setName("uploadFormElement");
				upload.setStyleName("x-btn");
				panel.add(upload);

				// Add a 'submit' button.
				com.google.gwt.user.client.ui.Button submit = new com.google.gwt.user.client.ui.Button("Submit", new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						form.submit();
					}
				});

				submit.setStyleName("x-btn");
				panel.add(submit);

				form.addSubmitHandler(new FormPanel.SubmitHandler() {

					public void onSubmit(SubmitEvent event) {

					}
				});

				form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {

					@Override
					public void onSubmitComplete(SubmitCompleteEvent event) {
						window.hide();
						String result = event.getResults();
						result = result.replaceAll("THISISABRACKETSUBSTITUTE1", "<");
						result = result.replaceAll("THISISABRACKETSUBSTITUTE2", ">");

						// the following if-block is needed because of some
						// compiled-mode problems
						if (result.contains("<pre>")) {
							result = result.replaceAll("<pre>", "");
							result = result.replaceAll("</pre>", "");
						}

						// the following if-block is needed because of some
						// parser problems
						if (result.contains("nbsp")) {
							result = result.replaceAll("&nbsp;", " ");
						}

						String template = myMapInfo.getTemplateName();
						MVController newController = (MVController) ArgumentMapMenuBar.this.myMapSpace.getSession().getController();

						// new XMLLoader(myMapSpace, ArgumentMapMenuBar.this.myMapSpace.getSession().getController(), result);
					}
				});
				window.setSize(240, 70);
				window.setHeading("Choose XML-File");
				window.add(form);
				window.show();
			}
		});
		return loadItem;
	}

	private Menu createParseMenu() {
		Menu menu = new Menu();
		MenuItem argunautItem = null;
		if (myMapInfo.getOntologyName().equalsIgnoreCase("argunaut")) {
			argunautItem = createFileUploadItem();
			menu.add(argunautItem);
		}

		else if (myMapInfo.getOntologyName().equalsIgnoreCase("largo")) {
			MenuItem largoItem = createLoadFromXmlFileItem();
			menu.add(largoItem);
		}
		return menu;
	}

	private MenuItem createFileUploadItem() {
		MenuItem fileUploadItem = new MenuItem("File upload");
		fileUploadItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent me) {
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				// The dialog window
				final Window window = new Window();

				// Create a FormPanel and point it at a service.
				final FormPanel form = new FormPanel();
				form.setAction(GWT.getModuleBaseURL() + "fileupload");

				// set the form to use the POST method, and multipart MIME
				// encoding for file upload
				form.setEncoding(FormPanel.ENCODING_MULTIPART);
				form.setMethod(FormPanel.METHOD_POST);

				// Create a panel to hold all of the form widgets.
				VerticalPanel panel = new VerticalPanel();
				form.setWidget(panel);

				// Create a FileUpload widget.
				final FileUpload upload = new FileUpload();
				upload.setName("uploadFormElement");
				upload.setStyleName("x-btn");
				panel.add(upload);

				// Add a 'submit' button.
				com.google.gwt.user.client.ui.Button submit = new com.google.gwt.user.client.ui.Button("Submit", new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						form.submit();
					}
				});
				submit.setStylePrimaryName("x-btn");
				panel.add(submit);

				form.addSubmitHandler(new FormPanel.SubmitHandler() {

					public void onSubmit(SubmitEvent event) {

					}
				});

				form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {

					@Override
					public void onSubmitComplete(SubmitCompleteEvent event) {
						window.hide();
						String result = event.getResults();
						result = result.replaceAll("THISISABRACKETSUBSTITUTE1", "<");
						result = result.replaceAll("THISISABRACKETSUBSTITUTE2", ">");

						// the following if-block is needed because of some
						// compiled-mode problems
						if (result.contains("<pre>")) {
							result = result.replaceAll("<pre>", "");
							result = result.replaceAll("</pre>", "");
						}

						// the following if-block is needed because of some
						// parser problems
						if (result.contains("nbsp")) {
							result = result.replaceAll("&nbsp;", " ");
						}

						MVController newController = (MVController) ArgumentMapMenuBar.this.myMapSpace.getSession().getController();
						ArgunautParser parser = new ArgunautParser(result, (ArgumentMapSpace) myMapSpace, newController);
						try {
							if (result.trim().equals("")) {
								MessageBox.info("No gml text found", "Please enter a proper gml-file text!", null);
							} else {
								parser.parseText();
							}
						} catch (DOMParseException e) {
							MessageBox.alert("Error", "The gml-file does not match the argunaut pattern!\n " + e.toString(), null);
							e.printStackTrace();
						} catch (Exception e) {
							MessageBox.alert("Unknown Error",
									"An error occured in class ArgumentMapMenuBar.java. Contact the system developer for further information.\n "
											+ e.toString(), null);
							MessageBox.alert("Unknown Error", e.toString(), null);
							e.printStackTrace();
						}
					}
				});
				window.setSize(240, 70);
				window.setHeading("Choose XML-File");
				window.add(form);
				window.show();

			}
		});
		return fileUploadItem;
	}

	private MenuItem createLoadFromXmlFileItem() {
		MenuItem LARGOItem = new MenuItem("Load from XML-File");
		LARGOItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent me) {
				ArgumentMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				// The dialog window
				final Window window = new Window();

				// Create a FormPanel and point it at a service.
				final FormPanel form = new FormPanel();
				form.setAction(GWT.getModuleBaseURL() + "fileupload");

				// set the form to use the POST method, and multipart MIME
				// encoding for file upload
				form.setEncoding(FormPanel.ENCODING_MULTIPART);
				form.setMethod(FormPanel.METHOD_POST);

				// Create a panel to hold all of the form widgets.
				VerticalPanel panel = new VerticalPanel();
				form.setWidget(panel);

				// Create a FileUpload widget.
				final FileUpload upload = new FileUpload();
				upload.setName("uploadFormElement");
				upload.setStyleName("x-btn");
				panel.add(upload);

				// Add a 'submit' button.
				com.google.gwt.user.client.ui.Button submit = new com.google.gwt.user.client.ui.Button("Submit", new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						form.submit();
					}
				});
				submit.setStyleName("x-btn");
				panel.add(submit);

				form.addSubmitHandler(new FormPanel.SubmitHandler() {

					public void onSubmit(SubmitEvent event) {

					}
				});

				form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {

					@Override
					public void onSubmitComplete(SubmitCompleteEvent event) {
						window.hide();
						String result = event.getResults();
						result = result.replaceAll("THISISABRACKETSUBSTITUTE1", "<");
						result = result.replaceAll("THISISABRACKETSUBSTITUTE2", ">");

						// the following if-block is needed because of some
						// compiled-mode problems
						if (result.contains("<pre>")) {
							result = result.replaceAll("<pre>", "");
							result = result.replaceAll("</pre>", "");
						}

						// the following if-block is needed because of some
						// parser problems
						if (result.contains("nbsp")) {
							result = result.replaceAll("&nbsp;", " ");
						}

						String template = myMapInfo.getTemplateName();
						MVController newController = (MVController) ArgumentMapMenuBar.this.myMapSpace.getSession().getController();

						LARGOParser parser = new LARGOParser(result, template, (ArgumentMapSpace) myMapSpace, newController);
						try {
							if (result.trim().equals("")) {
								MessageBox.info("No xml text found", "Please enter a proper xml-file text!", null);
							} else {
								parser.parseText();
							}
						} catch (DOMParseException e) {
							MessageBox.alert("Error", "The xml-file does not match the largo pattern!", null);
							e.printStackTrace();
						} catch (Exception e) {
							MessageBox
									.alert("Unknown Error",
											"An error occured in class ArgumentMapMenuBar.java. Contact the system developer for further information.",
											null);
							e.printStackTrace();
						}
					}
				});
				window.setSize(240, 70);
				window.setHeading("Choose XML-File");
				window.add(form);
				window.show();

			}
		});

		return LARGOItem;
	}

}
