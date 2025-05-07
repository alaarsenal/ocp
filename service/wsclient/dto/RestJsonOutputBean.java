package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RestJsonOutputBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Les messages de retour d'un service, le cas échéant.
     */
    @JsonProperty("messagesRetour")
    private Wrapper wrapper = new Wrapper();


    public RestJsonOutputBean() {//NOSONAR

    }

    @JsonIgnore
    public List<MessageRetour> getMessagesRetour() {
        return wrapper.getItem();
    }

    public void setMessagesRetour(List<MessageRetour> messagesRetour) {
        wrapper.setItem(messagesRetour);
    }

    private static class Wrapper {
        @JsonProperty("item")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<MessageRetour> item = new ArrayList<>();

        public List<MessageRetour> getItem() {
            return item;
        }

        public void setItem(List<MessageRetour> item) {
            this.item = item;
        }
    }

}
