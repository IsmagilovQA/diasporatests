package ua.net.itlabs.diaspora;

import org.junit.Before;
import steps.Relation;
import org.junit.BeforeClass;
import org.junit.Test;
import pages.*;
import ua.net.itlabs.BaseTest;

import static core.helpers.UniqueDataHelper.*;
import static pages.Aspects.*;
import static ua.net.itlabs.testDatas.Users.*;
import static core.Gherkin.*;

public class BasicOperationsTest extends BaseTest {

    @BeforeClass
    public static void givenSetupUsersRelation() {

        Relation.forUser(Pod1.eve).notToUsers(Pod1.ana, Pod1.rob).ensure();
        Relation.forUser(Pod1.rob).toUser(Pod1.ana, FRIENDS).notToUsers(Pod1.eve).ensure();
        Relation.forUser(Pod1.ana).toUser(Pod1.rob, FRIENDS).notToUsers(Pod1.eve).ensure();

    }

    @Before
    public void addGivenDescriptionToAllure() {

        GIVEN("Eve is not linked with Ana and Rob");
        GIVEN("Rob is linked with Ana in Friends aspect and unlinked with Eve");
        GIVEN("Ana is linked with Rob in Friends aspect and unlinked with Eve");

    }

    @Test
    public void testAlienPostCannotBeDeleted() {

        GIVEN("Public post from author exists");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.ensurePublicPost(Pod1.ana, the("Ana public"));

        EXPECT("Post is shown in user's stream and cannot be deleted");
        Diaspora.ensureSignInAs(Pod1.rob);
        Feed.assertPostCanNotBeDeleted(Pod1.ana, the("Ana public"));

    }

    @Test
    public void testDeletePost() {

        GIVEN("Public post from author exists");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.ensurePublicPost(Pod1.ana, the("Ana public"));

        WHEN("Post is deleted by author");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.deletePost(Pod1.ana, the("Ana public"));

        THEN("Post is not shown in author's stream");
        Feed.assertNoPost(Pod1.ana, the("Ana public"));

        EXPECT("Deleted post is not shown in linked user's stream");
        Diaspora.ensureSignInAs(Pod1.rob);
        Feed.assertNoPost(Pod1.ana, the("Ana public"));

    }

    @Test
    public void testLikePost() {

        GIVEN("Public post is added by author");
        Diaspora.ensureSignInAs(Pod1.ana);
        Menu.openStream();
        Feed.ensurePublicPost(Pod1.ana, the("Ana public for likes"));

        EXPECT("Count of likes is zero for new post");
        Feed.assertNoLikes(Pod1.ana, the("Ana public for likes"));

        WHEN("Post is liked by unlinked user in Contact's stream");
        Diaspora.ensureSignInAs(Pod1.eve);
        Menu.search(Pod1.ana.fullName);
        Feed.toggleLikePost(Pod1.ana, the("Ana public for likes"));

        THEN("Count of likes is incremented");
        Feed.assertLikes(Pod1.ana, the("Ana public for likes"), 1);

        EXPECT("Information about likes is available by post author");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.assertLikes(Pod1.ana, the("Ana public for likes"), 1);

    }

    @Test
    public void testUnlikePost() {

        GIVEN("Public post is added by author and liked by user");
        Diaspora.ensureSignInAs(Pod1.ana);
        Menu.openStream();
        Feed.ensurePublicPost(Pod1.ana, the("Ana public to unlike"));
        Diaspora.ensureSignInAs(Pod1.rob);
        Feed.toggleLikePost(Pod1.ana, the("Ana public to unlike"));

        EXPECT("Count of likes is incremented");
        Feed.assertLikes(Pod1.ana, the("Ana public to unlike"), 1);

        WHEN("Post of author is liked by unlinked user");
        Feed.toggleLikePost(Pod1.ana, the("Ana public to unlike"));

        THEN("Count of likes is decremented");
        Feed.assertNoLikes(Pod1.ana, the("Ana public to unlike"));

        EXPECT("Information about likes is available by post author");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.assertNoLikes(Pod1.ana, the("Ana public to unlike"));

    }

    @Test
    public void testAddCommentToPost() {

        GIVEN("Limited post from author exists");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.ensureAspectPost(Pod1.ana, FRIENDS, the("Ana for friends"));

        WHEN("Comment for post of author is added by linked user");
        Diaspora.ensureSignInAs(Pod1.rob);
        Feed.addComment(Pod1.ana, the("Ana for friends"), the("Rob answer"));

        THEN("Comment is shown in stream");
        Feed.assertComment(Pod1.ana, the("Ana for friends"), Pod1.rob, the("Rob answer"));

        EXPECT("Added comment is shown in author's stream");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.assertComment(Pod1.ana, the("Ana for friends"), Pod1.rob, the("Rob answer"));

    }

    @Test
    public void testDeleteCommentByAuthorOfComment() {

        GIVEN("Public post from author exists, comment to post from linked user exist");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.ensurePublicPost(Pod1.ana, the("Ana public"));
        Diaspora.ensureSignInAs(Pod1.rob);
        Feed.ensureCommentForPost(Pod1.ana, the("Ana public"), Pod1.rob, the("Rob comment"));

        WHEN("Comment is deleted by user");
        Feed.deleteComment(Pod1.ana, the("Ana public"), Pod1.rob, the("Rob comment"));

        THEN("Comment is not shown in stream");
        Feed.assertNoComment(Pod1.ana, the("Ana public"), Pod1.rob, the("Rob comment"));

        EXPECT("Deleted comment is not shown in author's stream");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.assertNoComment(Pod1.ana, the("Ana public"), Pod1.rob, the("Rob comment"));

    }

    @Test
    public void testDeleteCommentByAuthorOfPost() {

        GIVEN("Public post from author exists, comment to post from linked user exist");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.ensurePublicPost(Pod1.ana, the("Ana public"));
        Diaspora.ensureSignInAs(Pod1.rob);
        Feed.ensureCommentForPost(Pod1.ana, the("Ana public"), Pod1.rob, the("Rob comment"));

        WHEN("Comment is deleted by author of post");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.deleteComment(Pod1.ana, the("Ana public"), Pod1.rob, the("Rob comment"));

        THEN("Comment is not shown in stream");
        Feed.assertNoComment(Pod1.ana, the("Ana public"), Pod1.rob, the("Rob comment"));

        EXPECT("Deleted comment is not shown in user's stream");
        Diaspora.ensureSignInAs(Pod1.rob);
        Feed.assertNoComment(Pod1.ana, the("Ana public"), Pod1.rob, the("Rob comment"));

    }

    @Test
    public void testAlienCommentToAlienPostCannotBeDeleted() {

        GIVEN("Public post from author exists, comment to post from linked user exist");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.ensurePublicPost(Pod1.ana, the("Ana public"));
        Diaspora.ensureSignInAs(Pod1.rob);
        Feed.ensureCommentForPost(Pod1.ana, the("Ana public"), Pod1.rob, the("Rob comment"));

        EXPECT("Alien comment to alien post cannot be deleted");
        Diaspora.ensureSignInAs(Pod1.eve);
        Menu.search(Pod1.ana.fullName);
        Feed.assertCommentCanNotBeDeleted(Pod1.ana, the("Ana public"), Pod1.rob, the("Rob comment"));

    }

    @Test
    public void testPostCannotBeResharedByAuthor() {

        GIVEN("Public post from author is exists");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.ensurePublicPost(Pod1.ana, the("Ana public"));

        EXPECT("Author cannot reshare their own posts");
        Feed.assertPostCanNotBeReshared(Pod1.ana, the("Ana public"));

    }

    @Test
    public void testLimitedPostCannotBeReshared() {

        GIVEN("Limited post from author is exists");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.ensureAspectPost(Pod1.ana, FRIENDS, the("Ana for friends"));
        Feed.ensurePublicPost(Pod1.ana, the("Ana for friends"));

        EXPECT("Limited post cannot be reshared by any user");
        Diaspora.ensureSignInAs(Pod1.rob);
        Feed.assertPostCanNotBeReshared(Pod1.ana, the("Ana public"));

    }

    @Test
    public void testResharePublicPost() {

        GIVEN("Public post from author is exists");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.ensurePublicPost(Pod1.ana, the("Ana public"));

        WHEN("Public post is reshared by user");
        Diaspora.ensureSignInAs(Pod1.rob);
        Feed.resharePost(Pod1.ana, the("Ana public"));

        THEN("New public post about original post is added by user");
        Feed.assertPost(Pod1.rob, the("Ana public"));

        EXPECT("Resharing post is public and is shown for unlinked user");
        Diaspora.ensureSignInAs(Pod1.eve);
        Menu.search(Pod1.rob.fullName);
        Feed.assertPost(Pod1.rob, the("Ana public"));

    }

    @Test
    public void testDeleteResharedPublicPost() {

        GIVEN("Public post from author is exists and reshared");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.ensurePublicPost(Pod1.ana, the("Ana public"));
        Diaspora.ensureSignInAs(Pod1.eve);
        Feed.ensureResharePublicPost(Pod1.ana, the("Ana public"), Pod1.eve);

        EXPECT("Reshared and resharing posts are shown in stream");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.assertPost(Pod1.ana, the("Ana public"));
        Menu.search(Pod1.eve.fullName);
        Feed.assertPost(Pod1.eve, the("Ana public"));

        WHEN("Reshared original post is deleted");
        Menu.openStream();
        Feed.deletePost(Pod1.ana, the("Ana public"));

        THEN("Reshared posts is not shown in stream");
        AND("Resharing post does not contain original information from reshared post");
        Menu.openStream();//without stream refresh checks works unstable
        Feed.assertNoPost(Pod1.ana, the("Ana public"));
        Menu.search(Pod1.eve.fullName);
        Feed.assertNoPost(Pod1.eve, the("Ana public"));

    }


    @Test
    public void testAddMentionPost() {

        WHEN("Post with mention about linked user is added by author");
        Diaspora.ensureSignInAs(Pod1.ana);
        Feed.addPublicPostWithMentionAbout(Pod1.rob, the("public mention"));
        Feed.assertPost(Pod1.ana, the("public mention"));//this check for wait moment when stream will be loaded

        THEN("Post is shown in mentions stream of this linked user");
        Diaspora.ensureSignInAs(Pod1.rob);
        Feed.assertPost(Pod1.ana, the("public mention"));
        NavBar.openMentions();
        Feed.assertPost(Pod1.ana, the("public mention"));

    }

}
