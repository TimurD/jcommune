/**
 * Copyright (C) 2011  JTalks.org Team
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
 */
package org.jtalks.jcommune.web.controller;

import org.jtalks.jcommune.model.entity.Branch;
import org.jtalks.jcommune.model.entity.CodeReview;
import org.jtalks.jcommune.model.entity.Topic;
import org.jtalks.jcommune.service.BranchService;
import org.jtalks.jcommune.service.CodeReviewService;
import org.jtalks.jcommune.service.LastReadPostService;
import org.jtalks.jcommune.service.TopicModificationService;
import org.jtalks.jcommune.service.exceptions.NotFoundException;
import org.jtalks.jcommune.web.dto.CodeReviewDto;
import org.jtalks.jcommune.web.dto.TopicDto;
import org.jtalks.jcommune.web.dto.json.JsonResponse;
import org.jtalks.jcommune.web.dto.json.JsonResponseStatus;
import org.jtalks.jcommune.web.util.BreadcrumbBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

/**
 * Serves code review management web requests
 *
 * @author Vyachesla Mishcheryakov
 * @see Topic
 */
@Controller
public class CodeReviewController {

    private static final String CODE_REVIEW_VIEW = "codeReviewForm";
    public static final String BRANCH_ID = "branchId";
    public static final String BREADCRUMB_LIST = "breadcrumbList";
    private static final String SUBMIT_URL = "submitUrl";
    private static final String TOPIC_DTO = "topicDto";
    private static final String REDIRECT_URL = "redirect:/topics/";
    
    private BranchService branchService;    
    private BreadcrumbBuilder breadcrumbBuilder;
    private TopicModificationService topicModificationService;
    private LastReadPostService lastReadPostService;
    private CodeReviewService codeReviewService;
    
    /**
     * @param branchService            the object which provides actions on
     *                                 {@link org.jtalks.jcommune.model.entity.Branch} entity
     * @param breadcrumbBuilder        to create Breadcrumbs for pages
     * @param topicModificationService the object which provides actions on
     *                                 {@link org.jtalks.jcommune.model.entity.Topic} entity
     * @param lastReadPostService      to perform post-related actions   
     * @param codeReviewService        to operate with {@link CodeReview} entities
     */
    @Autowired
    public CodeReviewController(BranchService branchService,
                                BreadcrumbBuilder breadcrumbBuilder,
                                TopicModificationService topicModificationService,
                                LastReadPostService lastReadPostService,
                                CodeReviewService codeReviewService) {
        this.branchService = branchService;
        this.breadcrumbBuilder = breadcrumbBuilder;
        this.topicModificationService = topicModificationService;
        this.lastReadPostService = lastReadPostService;
        this.codeReviewService = codeReviewService;
    }
    
    /**
     * This method turns the trim binder on. Trim binder
     * removes leading and trailing spaces from the submitted fields.
     * So, it ensures, that all validations will be applied to
     * trimmed field values only.
     *
     * @param binder Binder object to be injected
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /**
     * Shows page with form for new code review
     *
     * @param branchId {@link Branch} branch, where code review will be created
     * @return {@code ModelAndView} object with "codeReviewForm" view, new {@link TopicDto} and branch id
     * @throws NotFoundException when branch was not found
     */
    @RequestMapping(value = "/reviews/new", method = RequestMethod.GET)
    public ModelAndView showNewCodeReviewPage(@RequestParam(BRANCH_ID) Long branchId) throws NotFoundException {
        Branch branch = branchService.get(branchId);
        Topic topic = new Topic();
        TopicDto dto = new TopicDto(topic);
        dto.setNotifyOnAnswers(true);
        return new ModelAndView(CODE_REVIEW_VIEW)
                .addObject(TOPIC_DTO, dto)
                .addObject(BRANCH_ID, branchId)
                .addObject(SUBMIT_URL, "/reviews/new?branchId=" + branchId)
                .addObject(BREADCRUMB_LIST, breadcrumbBuilder.getNewTopicBreadcrumb(branch));
    }
    
    /**
     * Create code review from data entered in form
     *
     * @param topicDto object with data from form
     * @param result   {@link BindingResult} validation result
     * @param branchId branch, where topic will be created
     * @return {@code ModelAndView} object which will be redirect to forum.html
     * @throws NotFoundException when branch not found
     */
    @RequestMapping(value = "/reviews/new", method = RequestMethod.POST)
    public ModelAndView createCodeReview(@Valid @ModelAttribute TopicDto topicDto,
                                    BindingResult result,
                                    @RequestParam(BRANCH_ID) Long branchId) throws NotFoundException {
        Branch branch = branchService.get(branchId);
        if (result.hasErrors()) {
            return new ModelAndView(CODE_REVIEW_VIEW)
                    .addObject(TOPIC_DTO, topicDto)
                    .addObject(BRANCH_ID, branchId)
                    .addObject(SUBMIT_URL, "/reviews/new?branchId=" + branchId)
                    .addObject(BREADCRUMB_LIST, breadcrumbBuilder.getForumBreadcrumb(branch));
        }
        Topic topic = topicDto.getTopic();
        topic.setBranch(branch);
        Topic createdTopic = topicModificationService.createCodeReview(
                topic, topicDto.getBodyText(), true);

        lastReadPostService.markTopicAsRead(createdTopic);
        return new ModelAndView(REDIRECT_URL + createdTopic.getId());
    }
    
    /**
     * Returns code review by its ID as JSON data
     * @param reviewId ID of code review
     * @return JSON response object containing string status and review DTO as 
     *          result field 
     * @throws NotFoundException if code review was not found
     */
    @RequestMapping(value="/reviews/{reviewId}/json", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getCodeReview(@PathVariable("reviewId") Long reviewId) throws NotFoundException {
        CodeReview review = codeReviewService.get(reviewId);
        return new JsonResponse(JsonResponseStatus.Success, new CodeReviewDto(review));
    }
    
}
