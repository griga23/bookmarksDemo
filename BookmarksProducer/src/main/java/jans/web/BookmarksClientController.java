package jans.web;

import jans.service.BookmarksService;
import jans.model.Bookmark;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class BookmarksClientController {

    //Kafka producer
    private final BookmarksService bookmarksService;

    // initialize Kafka producer service
    public BookmarksClientController(BookmarksService bookmarksService) {
        this.bookmarksService = bookmarksService;
    }

    // show bookmark input dialog for some user
    @RequestMapping(value = "/bookmarksProducer/{user}", method = RequestMethod.GET)
    public String showBookmarks(@PathVariable String user, Model uiModel) {
        uiModel.addAttribute("newBookmark", new Bookmark(user));
        return "bookmarksProducer";
    }

    // do action on the entered bookmark entry for some user
    @RequestMapping(value = "/saveBookmark", method = RequestMethod.POST)
    public String saveBookmark(@ModelAttribute Bookmark bookmark, BindingResult errors, Model model) {

        bookmark.setAdded(new Date());
        if (bookmark.getUrl()==null){
            bookmark.setAction("DELETE");
        }else{
            bookmark.setAction("SAVE");
        }
        bookmarksService.sendBookmark(bookmark);

        model.addAttribute("name", bookmark.getName());
        model.addAttribute("user", bookmark.getUser());
        return "bookmarkSaved";
    }

}
