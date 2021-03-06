package com.example.springblog.controllers;

import com.example.springblog.models.Post;
import com.example.springblog.models.User;
import com.example.springblog.repositories.PostRepository;
import com.example.springblog.repositories.Users;
import com.example.springblog.services.EmailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PostController {
    private final PostRepository postDao;
    private final Users userDao;
    private final EmailService emailService;

    public PostController(PostRepository postDao, Users userDao, EmailService emailService) {
        this.postDao = postDao;
        this.userDao = userDao;
        this.emailService = emailService;
    }

    @GetMapping("/posts")
    public String postsIndex(Model model) {
        model.addAttribute("posts", postDao.findAll());
        return "posts/index";
    }

    @GetMapping("/posts/{id}")
    public String postView(@PathVariable long id, Model model) {
        model.addAttribute("post", postDao.getOne(id));
        return "posts/show";
    }

    @GetMapping("/posts/{id}/edit")
    public String editView(@PathVariable long id, Model model) {
        model.addAttribute("post", postDao.getOne(id));
        return "posts/edit";
    }

    @PostMapping("/posts/{id}/edit")
    public String updatePost(@PathVariable long id, @ModelAttribute Post post) {
        User user = userDao.getOne((long) 1);
        post.setUser(user);
        postDao.save(post);
        return "redirect:/posts";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable long id) {
        postDao.deleteById(id);
        return "redirect:/posts";
    }

    @GetMapping("/posts/create")
    public String postForm(Model model) {
        model.addAttribute("post", new Post());
        return "posts/create";
    }

    @PostMapping("/posts/create")
    public String createPost(@RequestParam String title, @RequestParam String body)
    {
        Post post = new Post();
        post.setTitle(title);
        post.setBody(body);

        User user;

        user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        post.setUser(user);

        postDao.save(post);
        emailService.prepareAndSend(
                post,
                "Created post",
                "Created post"
        );
        return "redirect:/posts/";
    }
}
