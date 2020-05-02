package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateInputException;
import ru.sovaowltv.exceptions.JsonResponseException;
import ru.sovaowltv.exceptions.chat.BadSmileServiceException;
import ru.sovaowltv.exceptions.chat.BadSmileServiceParamsException;
import ru.sovaowltv.exceptions.chat.SavedSmileNotFound;
import ru.sovaowltv.exceptions.stream.NotYourStreamException;
import ru.sovaowltv.exceptions.stream.StreamNotFoundException;
import ru.sovaowltv.exceptions.user.UserNotFoundException;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
@Controller
@RequiredArgsConstructor
public class ExceptionsController implements ErrorController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @GetMapping("/error")
    @PostMapping("/error")
    public ModelAndView handleError(HttpServletRequest httpServletRequest) {
        Optional<User> userOptionalFromContext = Optional.empty();
        try {
            log.debug(httpServletRequest.toString());
            ModelAndView model = new ModelAndView("error/error");
            userOptionalFromContext = userUtil.getUserOptionalFromContext();
            userOptionalFromContext.ifPresent(user -> model.getModelMap().addAttribute("user", user));
            return model;
        } finally {
            userOptionalFromContext.ifPresent(usersRepositoryHandler::free);
        }
    }

    @ExceptionHandler(value = {UserNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView getUserNotFoundException(UserNotFoundException e) {
        Optional<User> userOptionalFromContext = Optional.empty();
        try {
            ModelAndView model = new ModelAndView("error/userNotFoundException");
            model.getModelMap().addAttribute("nick", e.getMessage());
            userOptionalFromContext = userUtil.getUserOptionalFromContext();
            userOptionalFromContext.ifPresent(user -> model.getModelMap().addAttribute("user", user));
            return model;
        } finally {
            userOptionalFromContext.ifPresent(usersRepositoryHandler::free);
        }
    }

    @ExceptionHandler(value = {StreamNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView getStreamNotFoundException(StreamNotFoundException e) {
        Optional<User> userOptionalFromContext = Optional.empty();
        try {
            ModelAndView model = new ModelAndView("error/streamNotFoundException");
            model.getModelMap().addAttribute("nick", e.getMessage());
            userOptionalFromContext = userUtil.getUserOptionalFromContext();
            userOptionalFromContext.ifPresent(user -> model.getModelMap().addAttribute("user", user));
            return model;
        } finally {
            userOptionalFromContext.ifPresent(usersRepositoryHandler::free);
        }
    }

    @ExceptionHandler(value = {TemplateInputException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object getTemplateInputException(TemplateInputException e) {
        return e.getMessage();
    }

    @ExceptionHandler(value = {JsonResponseException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Object getJsonResponseException(JsonResponseException e) {
        return e.getMessage();
    }
}
