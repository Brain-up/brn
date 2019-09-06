package com.epam.brn.spring;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

@SuppressWarnings("serial")
@Route("")
@PWA(name = "EPAM for kids", shortName = "EPAM for kids")
public class MainView extends VerticalLayout {

    HorizontalLayout panelLeft = new HorizontalLayout() {
        Button buttonGo = new Button("Go") {
            {
                this.getElement().setAttribute("onclick", "document.getElementsByTagName('audio')[0].play()");
                // TODO check if there may be a more Vaadin-style approach 
                // TODO check whether audio is supported; disable the button and issue an error message if not
                // NOTE with audio being disabled at OS level, Chrome does not yield any errors
            }
        };
        Element audio = new Element("audio") {
            {
                this.setAttribute("src", "/audio/t-rex-roar.mp3");
            }
        };
        {
            this.add(buttonGo);
            this.getElement().appendChild(audio);
            this.setAlignItems(Alignment.CENTER);
        }
    };
    HorizontalLayout panelTop = new HorizontalLayout() {
        {
            this.setWidthFull();
            this.getStyle()
                .set("margin-bottom", "1em")
                ;
            this.add(createApple(), createApple());
        }
    };
    Image imageCylinderRed = new Image("/svg/cylinder.svg", "Круглая фишка красная") {
        {
            this.getStyle()
                .set("position", "absolute")
                .set("margin", "0px") // a hack to override default setting as :host([theme~="spacing"]) ::slotted(*) { margin-top: 1em; }
                .set("width", "100px")
                .set("top", "20%")
                .set("left", "20%")
                ;
            this.addClickListener(event -> {
                        Notification.show("Cylinder clicked!");
                    });
        }
    };
    VerticalLayout panelBoard = new VerticalLayout() {
        {
            this.setSizeFull();
            this.getStyle()
                .set("position", "relative") // in order to allow absolute positioning for children
                .set("padding", "0px")       // fix default CSS to allow correct positioning for children
                .set("flex-grow", "1")
                ;
            for (int i = 0; i < 5; ++i) {
                this.add(createRowPanel());
            }
            this.add(imageCylinderRed);
        }
    };
    VerticalLayout panelTopAndBoard = new VerticalLayout() {
        {
            this.setSizeFull();
            this.add(panelTop, panelBoard);
        }
    };
    HorizontalLayout panelAutoCenterH = new HorizontalLayout() {
        {
            this.setSizeFull();
            this.setAlignItems(Alignment.CENTER);
            this.add(panelLeft, panelTopAndBoard);
        }
    };

    public MainView() {
        this.setSizeFull();
        this.setAlignItems(Alignment.CENTER);
        this.add(panelAutoCenterH);
    }

    Image createApple() {
        Image apple = new Image("/svg/apple.svg", "Яблоко");
        apple.getStyle().set("width", "3%");
        return apple;
    }

    HorizontalLayout createRowPanel() {
        HorizontalLayout rowPanel = new HorizontalLayout();
        rowPanel.setSizeFull();
        rowPanel.getStyle().set("margin", "0px");
        for (int j = 0; j < 7; ++j) {
            rowPanel.add(createBoardCell());
        }
        return rowPanel;
    }

    VerticalLayout createBoardCell() {
        VerticalLayout cell = new VerticalLayout();
        cell.setSizeFull();
        cell.getStyle()
            .set("margin", "0px")
            .set("padding", "0px")
            .set("border", "solid lightgray 3px")
            ;
        return cell;
    }

}
