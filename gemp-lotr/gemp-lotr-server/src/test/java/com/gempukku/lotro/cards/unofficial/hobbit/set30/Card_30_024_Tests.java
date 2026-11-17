package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_024_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dawn", "30_24");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion, vitality 4; a fellowship ability, a discard
					// bar that needs 2 [dwarven] followers, and a discount on Thorin
					put("gandalf", "30_26");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; +2 only with 3 Ents
					// Tom, Troll of Ettenmoors: [troll] Troll, vitality 4, fierce; "To play, discard an Orc", a discount on
					// Trolls, and +1 to each Troll when a companion loses a skirmish
					put("tom", "31_58");
					// Demolition Troll: [moria] Troll, vitality 3; a skirmish ability only
					put("demolition", "32_31");
					// Granite Giant: [moria] Giant, no Troll, vitality 3; a Shadow ability from the discard pile and an archery ability
					put("giant", "33_42");
					// Watchful Orc: [gundabad] Orc, vitality 3; a play trigger and a Shadow ability only
					put("watchful", "30_40");
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, vitality 2, fierce; a skirmish ability only
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DawnTakeYouAllStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Dawn Take You All!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>.<br>Exert Gandalf twice to wound a minion twice or to discard a Troll.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dawn");

		assertEquals("Dawn Take You All!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfTwiceToWoundAMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bilbo = scn.GetRingBearer();
		var tom = scn.GetShadowCard("tom");
		var watchful = scn.GetShadowCard("watchful");
		var giant = scn.GetShadowCard("giant");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(dawn);
		assertInZone(Zone.HAND, dawn);

		scn.StartGame();
		scn.MoveMinionsToTable(tom, watchful, giant);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(tom));
		assertEquals(0, scn.GetWoundsOn(watchful));
		assertEquals(0, scn.GetWoundsOn(giant));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, tom);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.FreepsPlayAvailable(dawn));
		scn.FreepsPlayCard(dawn);
		assertEquals(2, scn.GetWoundsOn(gandalf));

		// A Troll is on the table, so either can be done.
		assertTrue(scn.FreepsChoiceAvailable("Wound a minion twice"));
		assertTrue(scn.FreepsChoiceAvailable("Discard a Troll"));
		scn.FreepsChoose("Wound a minion twice");
		// Minions of three cultures and races, two of them in no skirmish; not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(tom, watchful, giant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, bilbo));
		scn.FreepsChooseCard(tom);

		// Twice: vitality 4, so he lives.
		assertEquals(2, scn.GetWoundsOn(tom));
		assertInZone(Zone.SHADOW_CHARACTERS, tom);
		assertEquals(0, scn.GetWoundsOn(watchful));
		assertEquals(0, scn.GetWoundsOn(giant));
		assertInZone(Zone.DISCARD, dawn);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionOfVitalityTwoIsKilledByTheTwoWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var tom = scn.GetShadowCard("tom");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(dawn);
		assertInZone(Zone.HAND, dawn);

		scn.StartGame();
		scn.MoveMinionsToTable(tom, spider);
		assertEquals(0, scn.GetWoundsOn(spider));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, tom);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPlayCard(dawn);
		scn.FreepsChoose("Wound a minion twice");
		// Two wounds kill a minion of vitality 2, which two exertions could not be asked of.
		assertTrue(scn.FreepsHasCardChoiceAvailable(tom, spider));
		scn.FreepsChooseCard(spider);

		assertInZone(Zone.DISCARD, spider);
		assertEquals(0, scn.GetWoundsOn(tom));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ExertsGandalfTwiceToDiscardATroll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bilbo = scn.GetRingBearer();
		var tom = scn.GetShadowCard("tom");
		var demolition = scn.GetShadowCard("demolition");
		var giant = scn.GetShadowCard("giant");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(dawn);
		assertInZone(Zone.HAND, dawn);

		scn.StartGame();
		scn.MoveMinionsToTable(tom, demolition, giant);
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, tom);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPlayCard(dawn);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		scn.FreepsChoose("Discard a Troll");
		// Trolls of two cultures, one in no skirmish; not a [moria] minion who is no Troll, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(tom, demolition));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(giant, gandalf, bilbo));
		scn.FreepsChooseCard(demolition);

		assertInZone(Zone.DISCARD, demolition);
		assertInZone(Zone.SHADOW_CHARACTERS, tom, giant);
		assertEquals(0, scn.GetWoundsOn(tom));
		assertEquals(0, scn.GetWoundsOn(giant));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoTrollTheWoundsAreWhatHappensUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var watchful = scn.GetShadowCard("watchful");
		var giant = scn.GetShadowCard("giant");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(dawn);
		assertInZone(Zone.HAND, dawn);

		scn.StartGame();
		scn.MoveMinionsToTable(watchful, giant);
		assertEquals(0, scn.GetWoundsOn(watchful));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, watchful);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPlayCard(dawn);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		// No choice between the two: the minion to wound is asked at once.
		assertFalse(scn.FreepsChoiceAvailable("Discard a Troll"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(watchful, giant));
		scn.FreepsChooseCard(watchful);

		assertEquals(2, scn.GetWoundsOn(watchful));
		assertEquals(0, scn.GetWoundsOn(giant));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoMinionAbleToTakeBothWoundsTheDiscardIsWhatHappensUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var tom = scn.GetShadowCard("tom");
		var demolition = scn.GetShadowCard("demolition");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(dawn);
		assertInZone(Zone.HAND, dawn);

		scn.StartGame();
		scn.MoveMinionsToTable(tom, demolition);
		// Each Troll has 1 vitality left: no minion can take both wounds.
		scn.AddWoundsToChar(tom, 3);
		scn.AddWoundsToChar(demolition, 2);
		assertEquals(3, scn.GetWoundsOn(tom));
		assertEquals(2, scn.GetWoundsOn(demolition));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, tom);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPlayCard(dawn);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsChoiceAvailable("Wound a minion twice"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(tom, demolition));
		scn.FreepsChooseCard(demolition);

		assertInZone(Zone.DISCARD, demolition);
		assertEquals(3, scn.GetWoundsOn(tom));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileGandalfCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var tom = scn.GetShadowCard("tom");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(dawn);
		assertInZone(Zone.HAND, dawn);

		scn.StartGame();
		scn.MoveMinionsToTable(tom);
		// Vitality 4 with 2 wounds: Gandalf can exert once, not twice. The [gandalf] companion beside him could.
		scn.AddWoundsToChar(gandalf, 2);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, tom);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(dawn));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
	}
}
