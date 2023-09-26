package example.domain.user.controller;

import example.domain.likes.entity.Likes;
import example.domain.likes.service.LikesService;
import example.domain.store.entity.Store;
import example.domain.store.repository.StoreRepository;
import example.domain.user.entity.User;
import example.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {

	private final StoreRepository storeRepository;
	private final LikesService likesService;
	private final UserRepository userRepository;

	@GetMapping("/user")
	public JSONObject oauthLoginInfo(Principal principal) {
		JSONObject obj = new JSONObject();
		try {
			String email = principal.getName();
			Optional<User> optionalUser = userRepository.findByEmail(email);
			if (optionalUser.isPresent()) {
				User users = optionalUser.get();
				obj.put("success", true);
				obj.put("name", users.getName());
				obj.put("email", users.getEmail());
				obj.put("nickname", users.getNickname());
				obj.put("picture", users.getPicture());
				obj.put("id", users.getId());
			}
			return obj;
		} catch (NullPointerException e) {
			obj.put("success", false);
			return obj;
		}
	}

	@GetMapping("/mypage")
	public ResponseEntity<Map<String, Object>> myPage(Principal principal){

		Map<String, Object> responseData = new HashMap<>();

			String email = principal.getName();
			Optional<User> optionalUser = userRepository.findByEmail(email);

		if (optionalUser == null) {
			// 로그인이 안 되어있는 상태면 로그인 페이지로
			// 예를 들어 로그인 페이지의 URL이 /login 이라고 가정하면 아래와 같이 리다이렉트할 수 있음
			return ResponseEntity.status(HttpStatus.FOUND)
					.header("Location", "/login")
					.build();
		}

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			responseData.put("name", user.getName());
			responseData.put("nickname", user.getNickname());
			responseData.put("email", user.getEmail());
			responseData.put("picture", user.getPicture());

			List<Likes> likeStore = likesService.getAllLikes();
			List<Store> likeStores = new ArrayList<>();

			for (Likes likesStore : likeStore) {
				Integer store_id = likesStore.getStore().getStoreid();
				System.out.println(store_id);
				Optional<Store> store = storeRepository.findByStoreid(store_id);

				if (store.isPresent()) {
					likeStores.add(store.get());
				}
			}
			responseData.put("likeStores", likeStores);
		}

		// 200 OK 상태 코드와 responseData를 JSON 형태로 반환
		return ResponseEntity.ok(responseData);
	}

	@PutMapping("/userInfo")
	public ResponseEntity<?> loginHandler(@RequestBody User user, Principal principal) {

		String email = principal.getName();
		Optional<User> optionalUser = userRepository.findByEmail(email);

		if (optionalUser.isPresent()) {
			User updateUser = optionalUser.get();
			updateUser.setSns_link(user.getSns_link());
			userRepository.save(updateUser);

			return ResponseEntity.ok("User Info Updated Successfully");
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
	}
}
