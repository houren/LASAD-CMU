package lasad.gwt.client.ui.workspace.graphmap;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lasad.gwt.client.LASAD_Client;
import lasad.gwt.client.constants.lasad_clientConstants;
import lasad.gwt.client.model.GraphMapInfo;
import lasad.gwt.client.model.ElementInfo;
import lasad.gwt.client.settings.DebugSettings;
import lasad.gwt.client.ui.box.AbstractBox;
import lasad.gwt.client.ui.link.AbstractLink;
import lasad.gwt.client.ui.link.AbstractLinkPanel;
import lasad.gwt.client.ui.workspace.LASADInfo;
import lasad.gwt.client.ui.workspace.graphmap.elements.AbstractCreateSpecialLinkDialog;
import lasad.gwt.client.ui.workspace.tableview.ArgumentEditionStyleEnum;
import lasad.shared.communication.objects.parameters.ParameterTypes;

import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;

import lasad.gwt.client.logger.Logger;

public abstract class GraphMapMenuBar extends ToolBar {
	
//	private final LASADActionSender communicator = LASADActionSender.getInstance();
//	private final ActionFactory actionBuilder = ActionFactory.getInstance();

	protected lasad_clientConstants myConstants = GWT.create(lasad_clientConstants.class);

	protected GraphMapSpace myMapSpace;
	protected GraphMapInfo myMapInfo;
	protected ArgumentEditionStyleEnum editionStyle;

	protected Button itemFeedback = null;

	/**
	 * Creates menu for a map
	 * 
	 * @param myMap
	 */
	public GraphMapMenuBar(GraphMapSpace mySpace) {
		this.myMapSpace = mySpace;
		this.myMapInfo = getDrawingAreaInformation() ;  //myMapSpace.getSession().getController().getMapInfo();
		this.editionStyle = LASAD_Client.getMapEditionStyle(String.valueOf(myMapInfo.getMapID()));

		createMenuBar();

		this.setSpacing(5);
		// To make the menu appear on top, even on top of a box
		DOM.setIntStyleAttribute(this.getElement(), "zIndex", XDOM.getTopZIndex() + 1);
	}
	
	/*
	 *  
	 */
	protected abstract GraphMapInfo getDrawingAreaInformation();
	public abstract void removeFeedbackEngine(String agentID, String typeID);
	public abstract void addFeedbackEngine(final String agentName, final String feedbackName);
	protected abstract void addFeedbackEngine(final String agentName, final String feedbackName, final String agentType);
	
	public abstract void createMenuBar();
	
//	private void createMenuBar() {
//
//		Button itemEdit = new Button(myConstants.EditMenu());
//		Button itemAdd = new Button(myConstants.AddMenu());
//		Button itemGroup = new Button(myConstants.GroupTools());
//
//		Menu editMenu = createEditMenu();
//		Menu addMenu = createAddMenu();
//		Menu groupMenu = createGroupMenu();
//		Menu debugMenu = createDebugMenu();
//
//		itemEdit.setMenu(editMenu);
//		itemAdd.setMenu(addMenu);
//		itemGroup.setMenu(groupMenu);
//
//		this.add(itemEdit);
//		this.add(itemAdd);
//
//		if (groupMenu.getItemCount() > 0) {
//			this.add(itemGroup);
//		}
//		
//		//TODO Zhenyu Geng
////		Button itemImage = new Button("Image upload");
////	    Menu imageMenu = createImageLoad();
////		itemImage.setMenu(imageMenu);
////		this.add(itemImage);
//
//		if (myMapInfo.isFeedback()) {
//			itemFeedback = new Button("Feedback");
//			Menu feedbackMenu = createFeedbackMenu();
//			itemFeedback.setMenu(feedbackMenu);
//			this.add(itemFeedback);
//		}
//
//		//Outsourced to Map Login Screen
////		if (myMapInfo.getOntologyName().equalsIgnoreCase("argunaut") || myMapInfo.getOntologyName().equalsIgnoreCase("largo")) {
////			Button itemParse = new Button("Load Map");
////			Menu parseMenu = createParseMenu();
////			itemParse.setMenu(parseMenu);
////			this.add(itemParse);
////		}
//
//		if (editionStyle == ArgumentEditionStyleEnum.TABLE) {
//			createZoomMenu();
//		}
//
//		if (LASAD_Client.getInstance().getRole().equalsIgnoreCase("developer")) {
//			Button itemDebug = new Button("Debug");
//			itemDebug.setMenu(debugMenu);
//			this.add(itemDebug);
//		}
//	}

	protected Menu createAddMenu() {
		Menu addMenu = new Menu();

		// Creates the sub menu for box types
		MenuItem addBoxMenu = new MenuItem(myConstants.ContributionMenuItem());
		addMenu.add(addBoxMenu);
		Menu subBoxes = new Menu();

		// Collect box types
		
		Map<String, ElementInfo> boxes = myMapInfo.getElementsByType("box");
		if(boxes != null) {
			for (ElementInfo info : boxes.values()) {
				subBoxes.add(createNewBoxItem(info));
			}
		}

		addBoxMenu.setSubMenu(subBoxes);

		// Creates the sub menu for link types
		MenuItem addLinkMenu = new MenuItem(myConstants.RelationMenuItem());
		addMenu.add(addLinkMenu);
		Menu subLinks = new Menu();

		// Collect link types
		Map<String, ElementInfo> relations = myMapInfo.getElementsByType("relation");
		if(relations != null) {
			for (ElementInfo info : relations.values()) {
				subLinks.add(createNewLinkItem(info));
			}
		}
		
		addLinkMenu.setSubMenu(subLinks);

		return addMenu;
	}
	
	private MenuItem createNewBoxItem(final ElementInfo currentElement) {

		MenuItem boxItem = new MenuItem(currentElement.getElementOption(ParameterTypes.Heading));
		boxItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent me) {
				handleCreateNewBoxItemSelectionEvent(me, currentElement);
//				int tempPosX = GraphMapMenuBar.this.getMyMapSpace().getMyMap().getHScrollPosition() + GraphMapMenuBar.this.getMyMapSpace().getMyMap().getInnerWidth() / 2 - (Integer.parseInt(currentElement.getUiOption(ParameterTypes.Width)) / 2);
//				int tempPosY = GraphMapMenuBar.this.getMyMapSpace().getMyMap().getVScrollPosition() + GraphMapMenuBar.this.getMyMapSpace().getMyMap().getInnerHeight() / 2 - (Integer.parseInt(currentElement.getUiOption(ParameterTypes.Height)) / 2);
//				GraphMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
//				communicator.sendActionPackage(actionBuilder.createBoxWithElements(currentElement, GraphMapMenuBar.this.getMyMapSpace().getMyMap().getID(), tempPosX, tempPosY));
			}
		});
		return boxItem;
	}
	
	/*
	 * method that handles last action of MenuItem boxItem's componentSelected event
	 */
	protected abstract void handleCreateNewBoxItemSelectionEvent(MenuEvent me, final ElementInfo currentElement);

	private MenuItem createNewLinkItem(final ElementInfo currentElement) {
		MenuItem linkItem = new MenuItem(currentElement.getElementOption(ParameterTypes.Heading));
		linkItem.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				Logger.log("Made it to component selected", Logger.DEBUG);
				TreeMap<String, AbstractBox> boxes = new TreeMap<String, AbstractBox>();
				TreeMap<String, AbstractLink> links = new TreeMap<String, AbstractLink>();
				List<Component> mapComponents = GraphMapMenuBar.this.getMyMapSpace().getMyMap().getItems();
				if (mapComponents.size() > 0) {
					for (Component component : mapComponents) {
						if (component instanceof AbstractBox) {
							boxes.put(((AbstractBox) component).getConnectedModel().getValue(ParameterTypes.RootElementId), (AbstractBox) component);
						} else if (component instanceof AbstractLinkPanel) {
							links.put(((AbstractLinkPanel) component).getMyLink().getConnectedModel().getValue(ParameterTypes.RootElementId), ((AbstractLinkPanel) component).getMyLink());
						}
					}
					if (boxes.size() < 2) {
						LASADInfo.display("Error", "There are not enough elements to connect on the map.");
					} else {
						//CreateSpecialLinkDialog linkDialog = new CreateSpecialLinkDialog(currentElement, getMyMapSpace().getMyMap().getID(), boxes, links);
						AbstractCreateSpecialLinkDialog linkDialog = createSpecialLinkDialog(currentElement, getMyMapSpace().getMyMap().getID(), boxes, links);
						linkDialog.show();
					}
				}

			}

		});

		return linkItem;
	}
	protected abstract AbstractCreateSpecialLinkDialog createSpecialLinkDialog(ElementInfo config, String mapId, TreeMap<String, AbstractBox> boxes, TreeMap<String, AbstractLink> links);
	
	protected MenuItem createGroupPointerItem() {
		final MenuItem groupPointerItem = new MenuItem("Create group pointer");
		groupPointerItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent me) {
				GraphMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				if (GraphMapMenuBar.this.getMyMapSpace().getMyMap().getMyAwarenessCursorID() == -1) {
					int tempPosX = GraphMapMenuBar.this.getMyMapSpace().getMyMap().getHScrollPosition() + GraphMapMenuBar.this.getMyMapSpace().getMyMap().getInnerWidth() / 2;
					int tempPosY = GraphMapMenuBar.this.getMyMapSpace().getMyMap().getVScrollPosition() + GraphMapMenuBar.this.getMyMapSpace().getMyMap().getInnerHeight() / 2;
					handleGroupPointerItemSelectionEvent(me,tempPosX, tempPosY);
					//communicator.sendActionPackage(actionBuilder.createGroupCursor(GraphMapMenuBar.this.getMyMapSpace().getMyMap().getID(), LASAD_Client.getInstance().getUsername(), tempPosX, tempPosY));
				} else {
					LASADInfo.display("Error", "There is already a group pointer on the map.");
				}
			}
		});
		return groupPointerItem;
	}
	/*
	 * method that handles last action of MenuItem groupPointerItem's componentSelected event
	 *
	 */
	protected abstract void handleGroupPointerItemSelectionEvent(MenuEvent me, int tempPosX, int tempPosY);
	
	protected MenuItem createSetDebugSettingsItem() {
		MenuItem setDebugSettingsItem = new MenuItem("Set Debug Settings");
		
		String mode = "";
		Menu settings = new Menu();
		
		//DEBUG
		if (DebugSettings.isDebug())
			mode = "Disable";
		else
			mode = "Enable";
		MenuItem settings_debug = new MenuItem(mode + " standard debugging");
		settings_debug.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				if (DebugSettings.isDebug()) 
					((MenuItem)ce.getItem()).setText("Enable standard debugging");
				else
					((MenuItem)ce.getItem()).setText("Disable standard debugging");
				DebugSettings.setDebug(!DebugSettings.isDebug());
			}
		});
		settings.add(settings_debug);
		
		
		//DEBUG_DETAILS
		if (DebugSettings.isDebug_details())
			mode = "Disable";
		else
			mode = "Enable";
		MenuItem settings_debug_details = new MenuItem(mode + " detailed debugging");
		settings_debug_details.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				if (DebugSettings.isDebug_details()) 
					((MenuItem)ce.getItem()).setText("Enable detailed debugging");
				else
					((MenuItem)ce.getItem()).setText("Disable detailed debugging");
				DebugSettings.setDebug_details(!DebugSettings.isDebug_details());
			}
		});
		settings.add(settings_debug_details);
		
		//DEBUG_ERRORS
		if (DebugSettings.isDebug_errors())
			mode = "Disable";
		else
			mode = "Enable";
		MenuItem settings_debug_errors = new MenuItem(mode + " error debugging");
		settings_debug_errors.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				if (DebugSettings.isDebug_errors()) 
					((MenuItem)ce.getItem()).setText("Enable error debugging");
				else
					((MenuItem)ce.getItem()).setText("Disable error debugging");
				DebugSettings.setDebug_errors(!DebugSettings.isDebug_errors());
			}
		});
		settings.add(settings_debug_errors);
		
		
		//DEBUG_POLLING
		if (DebugSettings.isDebug_polling())
			mode = "Disable";
		else
			mode = "Enable";
		MenuItem settings_debug_polling = new MenuItem(mode + " polling debugging");
		settings_debug_polling.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				if (DebugSettings.isDebug_polling()) 
					((MenuItem)ce.getItem()).setText("Enable polling debugging");
				else
					((MenuItem)ce.getItem()).setText("Disable polling debugging");
				DebugSettings.setDebug_polling(!DebugSettings.isDebug_polling());
			}
		});
		settings.add(settings_debug_polling);

		
		setDebugSettingsItem.setSubMenu(settings);
		return setDebugSettingsItem; 
	}
	
	protected MenuItem createFindContributionItem() {
		final MenuItem findContributionItem = new MenuItem(myConstants.SearchMenuItem());
		findContributionItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent me) {
				GraphMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
				findContributionItem.getParentMenu().hide();
				final MessageBox box = MessageBox.prompt(myConstants.FindContributionTitle(), myConstants.FindContributionText());
				box.addCallback(new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent be) {
						if (be.getButtonClicked().getText().equalsIgnoreCase("OK")) {
							if (be.getValue() == null) {
								LASADInfo.display("Error", "No valid value entered.");
							} else {
								List<Component> mapComponents = GraphMapMenuBar.this.getMyMapSpace().getMyMap().getItems();
								String searchValue = be.getValue();
								boolean elementFound = false;
								int i = 0;

								while (elementFound == false && i < mapComponents.size()) {
									if (mapComponents.get(i) instanceof AbstractBox) {
										AbstractBox foundBox = (AbstractBox) mapComponents.get(i);
										if (foundBox.getConnectedModel().getValue(ParameterTypes.RootElementId).equals(searchValue)) {
											foundBox.getElement().scrollIntoView();
											foundBox.getMap().getLayoutTarget().dom.setScrollLeft(foundBox.getPosition(true).x - foundBox.getMap().getInnerWidth() / 2 + foundBox.getWidth() / 2);
											foundBox.getMap().getLayoutTarget().dom.setScrollTop(foundBox.getPosition(true).y - foundBox.getMap().getInnerHeight() / 2 + foundBox.getHeight() / 2);
											elementFound = true;
										}
									} else if (mapComponents.get(i) instanceof AbstractLinkPanel) {
										AbstractLinkPanel foundLinkPanel = ((AbstractLinkPanel) mapComponents.get(i));
										if (foundLinkPanel.getMyLink().getConnectedModel().getValue(ParameterTypes.RootElementId).equals(searchValue)) {
											foundLinkPanel.getMyLink().getMap().getLayoutTarget().dom.setScrollLeft(foundLinkPanel.getPosition(true).x - foundLinkPanel.getMyLink().getMap().getInnerWidth() / 2 + foundLinkPanel.getWidth() / 2);
											foundLinkPanel.getMyLink().getMap().getLayoutTarget().dom.setScrollTop(foundLinkPanel.getPosition(true).y - foundLinkPanel.getMyLink().getMap().getInnerHeight() / 2 + foundLinkPanel.getHeight() / 2);
											elementFound = true;
										}
									}
									i++;
								}
								if (!elementFound) {
									LASADInfo.display("Error", "There is no such contribution");
								}
							}
						}
					}
				});
			}
		});
		return findContributionItem;
	}
	
//	private MenuItem deleteFeedbackItem() {
//		final MenuItem deleteFeedbackItem = new MenuItem(myConstants.DeleteAllFeedbackMenuItem());
//		deleteFeedbackItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//			@Override
//			public void componentSelected(MenuEvent me) {
//				GraphMapMenuBar.this.getMyMapSpace().getMyMap().deleteAllFeedbackClusters();
//				GraphMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
//			}
//		});
//		return deleteFeedbackItem;
//	}
	
	public GraphMapSpace getMyMapSpace() {
		return myMapSpace;
	}

	public void setMyMapSpace(GraphMapSpace myMapSpace) {
		this.myMapSpace = myMapSpace;
	}
	
//	//TODO Zhenyu
//	private Menu createImageLoad() {
//		Menu imagemenu = new Menu();
//		// TODO Re-implement the undo function
//		// MenuItem editUndoItem = createEditUndoItem();
//		// menu.add(editUndoItem);
//		MenuItem imageload = createImageUpload();
//		imagemenu.add(imageload);
//		
//		return imagemenu;
//	}
//	//TODO Zhenyu
//	private MenuItem createImageUpload() {
//		final MenuItem imageLoad = new MenuItem("select an image.."); 
//		imageLoad.addSelectionListener(new SelectionListener<MenuEvent>() {
//			@Override
//			public void componentSelected(MenuEvent me) {
//				GraphMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
//				// The dialog window
//				final Window window = new Window();
//
//				// Create a FormPanel and point it at a service.
//				final FormPanel form = new FormPanel();
//				form.setAction(GWT.getModuleBaseURL() + "ImageUpload");
//
//				// set the form to use the POST method, and multipart MIME
//				// encoding for file upload
//				form.setEncoding(FormPanel.ENCODING_MULTIPART);
//				form.setMethod(FormPanel.METHOD_POST);
//
//				// Create a panel to hold all of the form widgets.
//				VerticalPanel panel = new VerticalPanel();
//				form.setWidget(panel);
//
//				// Create a FileUpload widget.
//				final FileUpload upload = new FileUpload();
//				upload.setName("uploadFormElement");
//				upload.setStyleName("x-btn");
//				panel.add(upload);
//
//				// Add a 'submit' button.
//				com.google.gwt.user.client.ui.Button submit = new com.google.gwt.user.client.ui.Button("Submit", new ClickHandler() {
//
//					@Override
//					public void onClick(ClickEvent event) {
//						form.submit();
//					}
//				});
//				submit.setStyleName("x-btn");
//				panel.add(submit);
//
//				form.addSubmitHandler(new FormPanel.SubmitHandler() {
//
//					public void onSubmit(SubmitEvent event) {
//
//					}
//				});
//
//				form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
//
//					@Override
//					public void onSubmitComplete(SubmitCompleteEvent event) {
//						window.hide();
//						ActionPackage ap = new ActionPackage();
//						Action a = new Action();
//						a.setCategory(Categories.Map);
//						a.setCmd(Commands.BackgroundImage);
//						a.addParameter(ParameterTypes.MapId, ""+myMapInfo.getMapID());
//						a.addParameter(ParameterTypes.BackgroundImageURL, GWT.getHostPageBaseURL()+"uploads/"+event.getResults());
//						ap.addAction(a);
//						
//						LASADActionSender.getInstance().sendActionPackage(ap);
//					}
//				});
//				window.setSize(240, 70);
//				window.setHeading("Choose an Image File");
//				window.add(form);
//				window.show();			
//			}
//		});
//		
//		return imageLoad;
//	}

//	private void createZoomMenu() {
//		Button zoom = new Button("Zoom");
//		Menu zoomMenu = new Menu();
//
//		for (final TableZoomEnum e : TableZoomEnum.values()) {
//			MenuItem item = new MenuItem(e.toString());
//			zoomMenu.add(item);
//
//			item.addSelectionListener(new SelectionListener<MenuEvent>() {
//
//				@Override
//				public void componentSelected(MenuEvent ce) {
//
//					AbstractArgumentMap argumentMap = myMapSpace.getMyMap();
//					if (argumentMap instanceof ArgumentMapTable) {
//
//						ArgumentMapTable argumentMapTable = (ArgumentMapTable) argumentMap;
//
//						argumentMapTable.resize(e);
//
//					}
//				}
//			});
//		}
//
//		zoom.setMenu(zoomMenu);
//		add(zoom);
//	}

//	private Menu createGroupMenu() {
//		Menu groupMenu = new Menu();
//
//		if (myMapInfo.isGroupPointer()) {
//			MenuItem groupPointerItem = createGroupPointerItem();
//			groupMenu.add(groupPointerItem);
//		}
//		return groupMenu;
//	}

//	private Menu createDebugMenu() {
//		Menu debugMenu = new Menu();
//
//		// Creates the sub menu for box types
//		MenuItem setDebugSettings = createSetDebugSettingsItem();
//		debugMenu.add(setDebugSettings);
//		
//		MenuItem editSpots = createEditAttributeItem();
//		debugMenu.add(editSpots);
//
//		MenuItem getBoxPairsOfDifferentUsers = getBoxPairsOfDifferentUsersItem();
//		debugMenu.add(getBoxPairsOfDifferentUsers);
//
//		return debugMenu;
//	}
	
//	private Menu createEditMenu() {
//		Menu menu = new Menu();
//		// TODO Re-implement the undo function
//		// MenuItem editUndoItem = createEditUndoItem();
//		// menu.add(editUndoItem);
//		MenuItem findContribution = createFindContributionItem();
//		menu.add(findContribution);
//
//		MenuItem saveItem = createSaveItem();
//		menu.add(saveItem);
//
////		MenuItem loadItem = createLoadItem();
////		menu.add(loadItem);
//
//		MenuItem logOutItem = createLogOutItem();
//		menu.add(logOutItem);
//
//		return menu;
//	}

//	private Menu createFeedbackMenu() {
//		Menu menu = new Menu();
//
//		MenuItem deleteFeedback = deleteFeedbackItem();
//		menu.add(deleteFeedback);
//
//		return menu;
//	}

//	private Menu createParseMenu() {
//		Menu menu = new Menu();
//		MenuItem argunautItem = null;
//		if (myMapInfo.getOntologyName().equalsIgnoreCase("argunaut")) {
//			argunautItem = createFileUploadItem();
//			menu.add(argunautItem);
//		}
//
//		else if (myMapInfo.getOntologyName().equalsIgnoreCase("largo")) {
//			MenuItem largoItem = createLoadFromXmlFileItem();
//			menu.add(largoItem);
//		}
//		return menu;
//	}

//	private MenuItem createFileUploadItem() {
//		MenuItem fileUploadItem = new MenuItem("File upload");
//		fileUploadItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//			@Override
//			public void componentSelected(MenuEvent me) {
//				GraphMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
//				// The dialog window
//				final Window window = new Window();
//
//				// Create a FormPanel and point it at a service.
//				final FormPanel form = new FormPanel();
//				form.setAction(GWT.getModuleBaseURL() + "fileupload");
//
//				// set the form to use the POST method, and multipart MIME
//				// encoding for file upload
//				form.setEncoding(FormPanel.ENCODING_MULTIPART);
//				form.setMethod(FormPanel.METHOD_POST);
//
//				// Create a panel to hold all of the form widgets.
//				VerticalPanel panel = new VerticalPanel();
//				form.setWidget(panel);
//
//				// Create a FileUpload widget.
//				final FileUpload upload = new FileUpload();
//				upload.setName("uploadFormElement");
//				upload.setStyleName("x-btn");
//				panel.add(upload);
//
//				// Add a 'submit' button.
//				com.google.gwt.user.client.ui.Button submit = new com.google.gwt.user.client.ui.Button("Submit", new ClickHandler() {
//
//					@Override
//					public void onClick(ClickEvent event) {
//						form.submit();
//					}
//				});
//				submit.setStylePrimaryName("x-btn");
//				panel.add(submit);
//
//				form.addSubmitHandler(new FormPanel.SubmitHandler() {
//
//					public void onSubmit(SubmitEvent event) {
//
//					}
//				});
//
//				form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
//
//					@Override
//					public void onSubmitComplete(SubmitCompleteEvent event) {
//						window.hide();
//						String result = event.getResults();
//						result = result.replaceAll("THISISABRACKETSUBSTITUTE1", "<");
//						result = result.replaceAll("THISISABRACKETSUBSTITUTE2", ">");
//
//						// the following if-block is needed because of some
//						// compiled-mode problems
//						if (result.contains("<pre>")) {
//							result = result.replaceAll("<pre>", "");
//							result = result.replaceAll("</pre>", "");
//						}
//
//						// the following if-block is needed because of some
//						// parser problems
//						if (result.contains("nbsp")) {
//							result = result.replaceAll("&nbsp;", " ");
//						}
//
//						MVController newController = GraphMapMenuBar.this.myMapSpace.getSession().getController();
//						ArgunautParser parser = new ArgunautParser(result, myMapSpace, newController);
//						try {
//							if (result.trim().equals("")) {
//								MessageBox.info("No gml text found", "Please enter a proper gml-file text!", null);
//							} else {
//								parser.parseText();
//							}
//						} catch (DOMParseException e) {
//							MessageBox.alert("Error", "The gml-file does not match the argunaut pattern!\n " + e.toString(), null);
//							e.printStackTrace();
//						} catch (Exception e) {
//							MessageBox.alert("Unknown Error", "An error occured in class GraphMapMenuBar.java. Contact the system developer for further information.\n " + e.toString(), null);
//							MessageBox.alert("Unknown Error", e.toString(), null);
//							e.printStackTrace();
//						}
//					}
//				});
//				window.setSize(240, 70);
//				window.setHeading("Choose XML-File");
//				window.add(form);
//				window.show();
//
//			}
//		});
//		return fileUploadItem;
//	}

//	private MenuItem createLoadFromXmlFileItem() {
//		MenuItem LARGOItem = new MenuItem("Load from XML-File");
//		LARGOItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//			@Override
//			public void componentSelected(MenuEvent me) {
//				GraphMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
//				// The dialog window
//				final Window window = new Window();
//
//				// Create a FormPanel and point it at a service.
//				final FormPanel form = new FormPanel();
//				form.setAction(GWT.getModuleBaseURL() + "fileupload");
//
//				// set the form to use the POST method, and multipart MIME
//				// encoding for file upload
//				form.setEncoding(FormPanel.ENCODING_MULTIPART);
//				form.setMethod(FormPanel.METHOD_POST);
//
//				// Create a panel to hold all of the form widgets.
//				VerticalPanel panel = new VerticalPanel();
//				form.setWidget(panel);
//
//				// Create a FileUpload widget.
//				final FileUpload upload = new FileUpload();
//				upload.setName("uploadFormElement");
//				upload.setStyleName("x-btn");
//				panel.add(upload);
//
//				// Add a 'submit' button.
//				com.google.gwt.user.client.ui.Button submit = new com.google.gwt.user.client.ui.Button("Submit", new ClickHandler() {
//
//					@Override
//					public void onClick(ClickEvent event) {
//						form.submit();
//					}
//				});
//				submit.setStyleName("x-btn");
//				panel.add(submit);
//
//				form.addSubmitHandler(new FormPanel.SubmitHandler() {
//
//					public void onSubmit(SubmitEvent event) {
//
//					}
//				});
//
//				form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
//
//					@Override
//					public void onSubmitComplete(SubmitCompleteEvent event) {
//						window.hide();
//						String result = event.getResults();
//						result = result.replaceAll("THISISABRACKETSUBSTITUTE1", "<");
//						result = result.replaceAll("THISISABRACKETSUBSTITUTE2", ">");
//
//						// the following if-block is needed because of some
//						// compiled-mode problems
//						if (result.contains("<pre>")) {
//							result = result.replaceAll("<pre>", "");
//							result = result.replaceAll("</pre>", "");
//						}
//
//						// the following if-block is needed because of some
//						// parser problems
//						if (result.contains("nbsp")) {
//							result = result.replaceAll("&nbsp;", " ");
//						}
//
//						String template = myMapInfo.getTemplateName();
//						MVController newController = GraphMapMenuBar.this.myMapSpace.getSession().getController();
//
//						LARGOParser parser = new LARGOParser(result, template, myMapSpace, newController);
//						try {
//							if (result.trim().equals("")) {
//								MessageBox.info("No gml text found", "Please enter a proper gml-file text!", null);
//							} else {
//								parser.parseText();
//							}
//						} catch (DOMParseException e) {
//							MessageBox.alert("Error", "The gml-file does not match the largo pattern!", null);
//							e.printStackTrace();
//						} catch (Exception e) {
//							MessageBox.alert("Unknown Error", "An error occured in class GraphMapMenuBar.java. Contact the system developer for further information.", null);
//							e.printStackTrace();
//						}
//					}
//				});
//				window.setSize(240, 70);
//				window.setHeading("Choose XML-File");
//				window.add(form);
//				window.show();
//
//			}
//		});
//
//		return LARGOItem;
//	}

//	private MenuItem createEditUndoItem() {
//		MenuItem editUndoItem = new MenuItem("Undo");
//		editUndoItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//			@Override
//			public void componentSelected(MenuEvent me) {
//				handleEditUndoItemEvent();
//				GraphMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
//				handleEditUndoItemEvent();
//				// Send Action --> Server
//				//communicator.sendActionPackage(actionBuilder.undo(GraphMapMenuBar.this.getMyMapSpace().getMyMap().getID()));
//			}
//		});
//		return editUndoItem;
//	}
//	/*
//	 * method that handles last action of MenuItem editUndoItem's componentSelected event
//	 *
//	 */
//	public abstract void handleEditUndoItemEvent();

//	private MenuItem createLogOutItem() {
//		final MenuItem logOutItem = new MenuItem("Logout");
//		logOutItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//			@Override
//			public void componentSelected(MenuEvent me) {
//				GraphMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
//				logOutItem.getParentMenu().hide();
//				LASAD_Client.getInstance().logOut();
//			}
//		});
//		return logOutItem;
//	}

//	private MenuItem createSaveItem() {
//		final MenuItem saveItem = new MenuItem("Export map...");
//		saveItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//			@Override
//			public void componentSelected(MenuEvent ce) {
//				MapToXMLConverter conv = new MapToXMLConverter(GraphMapMenuBar.this.getMyMapSpace().getMyMap(), GraphMapMenuBar.this.myMapInfo.getXmlOntology(), GraphMapMenuBar.this.myMapInfo.getXmlTemplate());
//				
//				// Send the xml-string as a post request to servlet
//				// Get the file name via http-response, then open file location
//				RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getModuleBaseURL() + "saveToXmlServlet");
//				RequestCallback handler = new RequestCallback() {
//
//					@Override
//					public void onError(Request request, Throwable e) {
//						if (DebugSettings.debug_errors)
//							Logger.log(e.toString(), Logger.DEBUG_ERRORS);
//					}
//
//					@Override
//					public void onResponseReceived(Request request, Response response) {
//						// Browser will open a save file dialog box
//						com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "saveToXmlServlet", "_blank", "enabled");
//					}
//				};
//				try {
//					builder.sendRequest(conv.getXmlString(), handler);
//				} catch (RequestException e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		return saveItem;
//	}

//	private MenuItem createLoadItem() {
//		final MenuItem loadItem = new MenuItem("Load Map");
//		loadItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//			@Override
//			public void componentSelected(MenuEvent ce) {
//				GraphMapMenuBar.this.getMyMapSpace().getMyMap().getFocusHandler().releaseAllFocus();
//				// The dialog window
//				final Window window = new Window();
//
//				// Create a FormPanel and point it at a service.
//				final FormPanel form = new FormPanel();
//				form.setAction(GWT.getModuleBaseURL() + "fileupload");
//
//				// set the form to use the POST method, and multipart MIME
//				// encoding for file upload
//				form.setEncoding(FormPanel.ENCODING_MULTIPART);
//				form.setMethod(FormPanel.METHOD_POST);
//
//				// Create a panel to hold all of the form widgets.
//				VerticalPanel panel = new VerticalPanel();
//				form.setWidget(panel);
//
//				// Create a FileUpload widget.
//				final FileUpload upload = new FileUpload();
//				upload.setName("uploadFormElement");
//				upload.setStyleName("x-btn");
//				panel.add(upload);
//
//				// Add a 'submit' button.
//				com.google.gwt.user.client.ui.Button submit = new com.google.gwt.user.client.ui.Button("Submit", new ClickHandler() {
//
//					@Override
//					public void onClick(ClickEvent event) {
//						form.submit();
//					}
//				});
//				submit.setStyleName("x-btn");
//				panel.add(submit);
//
//				form.addSubmitHandler(new FormPanel.SubmitHandler() {
//
//					public void onSubmit(SubmitEvent event) {
//
//					}
//				});
//
//				form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
//
//					@Override
//					public void onSubmitComplete(SubmitCompleteEvent event) {
//						window.hide();
//						String result = event.getResults();
//						result = result.replaceAll("THISISABRACKETSUBSTITUTE1", "<");
//						result = result.replaceAll("THISISABRACKETSUBSTITUTE2", ">");
//
//						// the following if-block is needed because of some
//						// compiled-mode problems
//						if (result.contains("<pre>")) {
//							result = result.replaceAll("<pre>", "");
//							result = result.replaceAll("</pre>", "");
//						}
//
//						// the following if-block is needed because of some
//						// parser problems
//						if (result.contains("nbsp")) {
//							result = result.replaceAll("&nbsp;", " ");
//						}
//
//						String template = myMapInfo.getTemplateName();
//						MVController newController = GraphMapMenuBar.this.myMapSpace.getSession().getController();
//
//						new XMLLoader(myMapSpace, GraphMapMenuBar.this.myMapSpace.getSession().getController(), result);
//					}
//				});
//				window.setSize(240, 70);
//				window.setHeading("Choose XML-File");
//				window.add(form);
//				window.show();
//			}
//		});
//		return loadItem;
//	}

//	private MenuItem createEditAttributeItem() {
//		final MenuItem editAttributeItem = new MenuItem("Edit Spots");
//		editAttributeItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//			@Override
//			public void componentSelected(MenuEvent me) {
//				editAttributeItem.getParentMenu().hide();
//				final MessageBox box = MessageBox.prompt("Edit Attribute", "Code-Pattern: Element-ID,Attribute,Value");
//				box.addCallback(new Listener<MessageBoxEvent>() {
//					public void handleEvent(MessageBoxEvent be) {
//						if (be.getButtonClicked().getText().equalsIgnoreCase("OK")) {
//							if (be.getValue() == null) {
//								LASADInfo.display("Error", "No valid value entered.");
//							} else {
//								String[] input = be.getValue().split(",");
//								String id = input[0];
//								String attribute = input[1];
//								String value = "";
//								if (input.length == 3) {
//									value = input[2];
//								}
//								//communicator.sendActionPackage(actionBuilder.editAttribute(GraphMapMenuBar.this.getMyMapSpace().getMyMap().getID(), id, attribute, value));
//							}
//						}
//					}
//				});
//			}
//		});
//		return editAttributeItem;
//	}

//	private MenuItem getBoxPairsOfDifferentUsersItem() {
//		final MenuItem editAttributeItem = new MenuItem("Count number of connected box pairs with different authors");
//		editAttributeItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//			@Override
//			public void componentSelected(MenuEvent me) {
//
//				MVController myController = LASAD_Client.getMVCController(myMapSpace.getMyMap().getID());
//
//				int number = 0;
//				// If object is a box, protect all links that are connected to
//				// the box as well as all boxes that are connected to these
//				// links
//				Iterator<UnspecifiedElementModel> myProtectionIterator = myController.getAllElements().iterator();
//				while (myProtectionIterator.hasNext()) {
//					UnspecifiedElementModel item = myProtectionIterator.next();
//					if (item.getParents().size() >= 2) {
//						if (!item.getParents().get(0).getAuthor().equalsIgnoreCase(item.getParents().get(1).getAuthor())) {
//							number++;
//						}
//					}
//				}
//
//				LASADInfo.display("Result", number + "");
//
//			}
//		});
//
//		return editAttributeItem;
//	}
	
//	public void addFeedbackEngine(final String agentName, final String feedbackName) {
//		if (itemFeedback != null) {
//			MenuItem newFeedbackAgentItem = new MenuItem(agentName + ": " + feedbackName);
//			newFeedbackAgentItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//				@Override
//				public void componentSelected(MenuEvent me) {
//					// Send Action --> Server
//					GraphMapMenuBar.this.getMyMapSpace().getMyMap().deleteAllFeedbackClusters();
//					communicator.sendActionPackage(actionBuilder.requestFeedback(GraphMapMenuBar.this.getMyMapSpace().getMyMap().getID(), LASAD_Client.getInstance().getUsername(), agentName, feedbackName));
//				}
//			});
//			itemFeedback.getMenu().add(newFeedbackAgentItem);
//		} else
//			Logger.log("FEEDBACK MENU IS NULL", Logger.DEBUG_ERRORS);
//	}

//	public void addFeedbackEngine(final String agentName, final String feedbackName, final String agentType) {
//		if (itemFeedback != null) {
//			MenuItem newFeedbackAgentItem = new MenuItem(agentName + ": " + feedbackName);
//			newFeedbackAgentItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//				@Override
//				public void componentSelected(MenuEvent me) {
//					// Send Action --> Server
//					GraphMapMenuBar.this.getMyMapSpace().getMyMap().deleteAllFeedbackClusters();
//					communicator.sendActionPackage(actionBuilder.requestFeedback(GraphMapMenuBar.this.getMyMapSpace().getMyMap().getID(), LASAD_Client.getInstance().getUsername(), agentName, feedbackName, agentType));
//				}
//			});
//			itemFeedback.getMenu().add(newFeedbackAgentItem);
//		} else
//			Logger.log("FEEDBACK MENU IS NULL", Logger.DEBUG_ERRORS);
//	}

//	public void removeFeedbackEngine(String agentID, String typeID) {
//		if (itemFeedback != null) {
//			for (int i = 0; i < itemFeedback.getMenu().getItemCount(); i++) {
//				MenuItem x = (MenuItem) itemFeedback.getMenu().getItem(i);
//				Logger.log("i = " + i, Logger.DEBUG);
//				Logger.log("MENUITEM TEXT: " + x.getText(), Logger.DEBUG);
//				Logger.log("AGENTID: " + agentID, Logger.DEBUG);
//				Logger.log("TYPEID:" + typeID, Logger.DEBUG);
//				if (x.getText().equals(agentID + ": " + typeID)) {
//					itemFeedback.getMenu().remove(x);
//					break;
//				}
//			}
//		}
//	}
}
