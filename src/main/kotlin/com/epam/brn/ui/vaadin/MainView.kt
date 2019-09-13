package com.epam.brn.ui.vaadin

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.dom.Element
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.PWA
import com.lifescience.brn.constant.BrnPath

@SuppressWarnings("serial")
@Route(BrnPath.UI_VAADIN_ROUTE)
@PWA(name = "EPAM auditory skills training tool", shortName = "EPAM BRN")
public class MainView : VerticalLayout() {

    val panelLeft = object : HorizontalLayout() {
        val buttonGo = object : Button("Go") {
            init {
                this.getElement().setAttribute("onclick", "document.getElementsByTagName('audio')[0].play()")
                // TODO check if there may be a more Vaadin-style approach 
                // TODO check whether audio is supported; disable the button and issue an error message if not
                // NOTE with audio being disabled at OS level, Chrome does not yield any errors
            }
        }
        val audio = object : Element("audio") {
            init {
                this.setAttribute("src", "/audio/t-rex-roar.mp3")
            }
        }
        init {
            this.add(buttonGo)
            this.getElement().appendChild(audio)
            this.setAlignItems(FlexComponent.Alignment.CENTER)
        }
    }
    val panelTop = object : HorizontalLayout() {
        init {
            this.setWidthFull()
            this.getStyle()
                .set("margin-bottom", "1em")
            this.add(createApple(), createApple())
        }
    }
    val imageCylinderRed = object : Image("/svg/cylinder.svg", "Круглая фишка красная") {
        init {
            this.getStyle()
                .set("position", "absolute")
                .set("margin", "0px") // a hack to override default setting as :host([theme~="spacing"]) ::slotted(*) { margin-top: 1em; }
                .set("width", "100px")
                .set("top", "20%")
                .set("left", "20%")
            this.addClickListener { _ -> Notification.show("Cylinder clicked!") }
        }
    }
    val panelBoard = object : VerticalLayout() {
        init {
            this.setSizeFull()
            this.getStyle()
                .set("position", "relative") // in order to allow absolute positioning for children
                .set("padding", "0px") // fix default CSS to allow correct positioning for children
                .set("flex-grow", "1")
            for (i in 1..5) {
                this.add(createRowPanel())
            }
            this.add(imageCylinderRed)
        }
    }
    val panelTopAndBoard = object : VerticalLayout() {
        init {
            this.setSizeFull()
            this.add(panelTop, panelBoard)
        }
    }
    val panelAutoCenterH = object : HorizontalLayout() {
        init {
            this.setSizeFull()
            this.setAlignItems(FlexComponent.Alignment.CENTER)
            this.add(panelLeft, panelTopAndBoard)
        }
    }

    init {
        this.setSizeFull()
        this.setAlignItems(FlexComponent.Alignment.CENTER)
        this.add(panelAutoCenterH)
    }

    private fun createApple(): Image {
        val apple = Image("/svg/apple.svg", "Яблоко")
        apple.getStyle().set("width", "3%")
        return apple
    }

    private fun createRowPanel(): HorizontalLayout {
        val rowPanel = HorizontalLayout()
        rowPanel.setSizeFull()
        rowPanel.getStyle().set("margin", "0px")
        for (j in 1..7) {
            rowPanel.add(*arrayOf(createBoardCell()))
        }
        return rowPanel
    }

    private fun createBoardCell(): VerticalLayout {
        val cell = VerticalLayout()
        cell.setSizeFull()
        cell.getStyle()
            .set("margin", "0px")
            .set("padding", "0px")
            .set("border", "solid lightgray 3px")
        return cell
    }
}
