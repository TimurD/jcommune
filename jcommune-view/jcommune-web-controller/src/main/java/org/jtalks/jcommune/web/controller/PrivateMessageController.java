/**
 * Copyright (C) 2011  jtalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Also add information on how to contact you by electronic and paper mail.
 * Creation date: Apr 12, 2011 / 8:05:19 PM
 * The jtalks.org Project
 */
package org.jtalks.jcommune.web.controller;

import org.jtalks.jcommune.model.entity.PrivateMessage;
import org.jtalks.jcommune.service.PrivateMessageService;
import org.jtalks.jcommune.service.exceptions.NotFoundException;
import org.jtalks.jcommune.web.dto.PrivateMessageDto;
import org.jtalks.jcommune.web.dto.PrivateMessageDtoBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

/**
 * MVC controller for Private Messaging. Handles request for inbox, outbox and new private messages.
 *
 * @author Pavel Vervenko
 * @author Max Malakhov
 * @author Kirill Afonin
 * @author Alexandre Teterin
 */
@Controller
public class PrivateMessageController {

    private final PrivateMessageService pmService;
    private PrivateMessageDtoBuilder pmDtoBuilder = new PrivateMessageDtoBuilder();

    //constants are moved here when occurs 4 or more times, as project PMD rule states
    private static final String PM_FORM = "pm/pmForm";
    private static final String PM_ID = "pmId";
    private static final String DTO = "privateMessageDto";

    /**
     * @param pmService the PrivateMessageService instance
     */
    @Autowired
    public PrivateMessageController(PrivateMessageService pmService) {
        this.pmService = pmService;
    }

    /**
     * This method allows us to set the DTO builder.
     * This can be useful for testing to mock/stub the real builder.
     *
     * @param pmDtoBuilder builder to be used when constructing DTO objects
     */
    public void setPmDtoBuilder(PrivateMessageDtoBuilder pmDtoBuilder) {
        this.pmDtoBuilder = pmDtoBuilder;
    }

    /**
     * Render the PM inbox page with the list of incoming messages for the /inbox URI.
     *
     * @return {@code ModelAndView} with added list of inbox messages
     */
    @RequestMapping(value = "/pm/inbox", method = RequestMethod.GET)
    public ModelAndView displayInboxPage() {
        return new ModelAndView("pm/inbox", "pmList", pmService.getInboxForCurrentUser());
    }

    /**
     * Render the PM outbox page with the list of sent messages for the /outbox URI.
     *
     * @return {@code ModelAndView} with added list of outbox messages
     */
    @RequestMapping(value = "/pm/outbox", method = RequestMethod.GET)
    public ModelAndView displayOutboxPage() {
        return new ModelAndView("pm/outbox", "pmList", pmService.getOutboxForCurrentUser());
    }

    /**
     * Render the page with a form for creation new Private Message with empty binded {@link PrivateMessageDto}.
     *
     * @return {@code ModelAndView} with the form
     */
    @RequestMapping(value = "/pm/new", method = RequestMethod.GET)
    public ModelAndView displayNewPMPage() {
        return new ModelAndView(PM_FORM, DTO, new PrivateMessageDto());
    }

    /**
     * Render the page with the form for the reply to original message.
     * The form has the next filled fields: recipient, title
     *
     * @param id {@link PrivateMessage} id
     * @return {@code ModelAndView} with the message having filled recipient, title fields
     * @throws org.jtalks.jcommune.service.exceptions.NotFoundException
     *          when message not found
     */
    @RequestMapping(value = "/pm/{pmId}/reply", method = RequestMethod.GET)
    public ModelAndView displayReplyPMPage(@PathVariable(PM_ID) Long id) throws NotFoundException {
        PrivateMessage pm = pmService.get(id);
        PrivateMessageDto object = pmDtoBuilder.getReplyDtoFor(pm);
        return new ModelAndView(PM_FORM, DTO, object);
    }

    /**
     * Render the page with the form for the reply with quoting to original message.
     * The form has the next filled fields: recipient, title, message
     *
     * @param id {@link PrivateMessage} id
     * @return {@code ModelAndView} with the message having filled recipient, title, message fields
     * @throws org.jtalks.jcommune.service.exceptions.NotFoundException
     *          when message not found
     */
    @RequestMapping(value = "/pm/{pmId}/quote", method = RequestMethod.GET)
    public ModelAndView displayQuotePMPage(@PathVariable(PM_ID) Long id) throws NotFoundException {
        PrivateMessage pm = pmService.get(id);
        PrivateMessageDto object = pmDtoBuilder.getQuoteDtoFor(pm);
        return new ModelAndView(PM_FORM, DTO, object);
    }


    /**
     * Save the PrivateMessage for the filled in PrivateMessageDto.
     *
     * @param pmDto  {@link PrivateMessageDto} populated in form
     * @param result result of {@link PrivateMessageDto} validation
     * @return redirect to /inbox on success or back to "/new_pm" on validation errors
     */
    @RequestMapping(value = "/pm/new", method = RequestMethod.POST)
    public String send(@Valid @ModelAttribute PrivateMessageDto pmDto, BindingResult result) {
        if (result.hasErrors()) {
            return PM_FORM;
        }
        try {
            if (pmDto.getId() > 0) {
                pmService.sendDraft(pmDto.getId(), pmDto.getTitle(), pmDto.getBody(), pmDto.getRecipient());
            } else {
                pmService.sendMessage(pmDto.getTitle(), pmDto.getBody(), pmDto.getRecipient());
            }
        } catch (NotFoundException nfe) {
            result.rejectValue("recipient", "label.wrong_recipient");
            return PM_FORM;
        }
        return "redirect:/pm/outbox.html";
    }

    /**
     * Show page with private message details.
     *
     * @param folder message folder (inbox/outbox/drafts)
     * @param id     {@link PrivateMessage} id
     * @return {@code ModelAndView} with a message
     * @throws org.jtalks.jcommune.service.exceptions.NotFoundException
     *          when message not found
     */
    @RequestMapping(value = "/pm/{folder}/{pmId}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable("folder") String folder,
                             @PathVariable(PM_ID) Long id) throws NotFoundException {

        PrivateMessage pm = pmService.get(id);
        if ("inbox".equals(folder)) {
            pmService.markAsRead(pm);
        }

        return new ModelAndView("pm/showPm", "pm", pm);
    }

    /**
     * Get list of current user's list of draft messages.
     *
     * @return {@code ModelAndView} with list of messages
     */
    @RequestMapping(value = "/pm/drafts", method = RequestMethod.GET)
    public ModelAndView displayDraftsPage() {
        return new ModelAndView("pm/drafts", "pmList", pmService.getDraftsFromCurrentUser());
    }

    /**
     * @param id {@link PrivateMessage} id
     * @return private messsage form view and populated form dto
     * @throws NotFoundException when message not found
     */
    @RequestMapping(value = "/pm/{pmId}/edit", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable(PM_ID) Long id) throws NotFoundException {
        PrivateMessage pm = pmService.get(id);
        if (!pm.isDraft()) {
            return new ModelAndView("pm/inbox");
        }
        return new ModelAndView(PM_FORM, DTO,
                new PrivateMessageDtoBuilder().getFullPmDtoFor(pm));
    }

    /**
     * Save private message as draft.
     *
     * @param pmDto  Dto populated in form
     * @param result validation result
     * @return redirect to "drafts" folder if saved successfully or show form with error message
     */
    @RequestMapping(value = "/pm/save", method = RequestMethod.POST)
    public String save(@Valid @ModelAttribute PrivateMessageDto pmDto, BindingResult result) {
        if (result.hasErrors()) {
            return PM_FORM;
        }
        try {
            pmService.saveDraft(pmDto.getId(), pmDto.getTitle(), pmDto.getBody(), pmDto.getRecipient());
        } catch (NotFoundException nfe) {
            result.rejectValue("recipient", "label.wrong_recipient");
            return PM_FORM;
        }
        return "redirect:/pm/drafts.html";
    }
}
