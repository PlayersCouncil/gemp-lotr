package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_037_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("home", "30_37");

					// Thorin, Oakenshield: [dwarven] Dwarf companion, cost 2, vitality 4; a Fellowship ability
					put("thorin", "30_19");
					// Glóin, Father of Gimli: [dwarven] Dwarf companion, cost 2, vitality 3; a Skirmish ability
					put("gloin", "30_11");
					// Balin, Brother of Dwalin: [dwarven] Dwarf companion; a Fellowship ability only
					put("balin", "30_2");
					// Gandalf, Leader of Dwarves: [gandalf] companion, cost 2; a reconcile draw and Skirmish abilities
					put("gandalf", "30_25");
					// Gandalf, Leader of the Company: [gandalf] companion, laid only in a dead pile here
					put("gandalf2", "30_28");
					// Iron Hills Army: [dwarven] Dwarf ALLY, cost 3; a Maneuver ability
					put("army", "32_5");
					// Dain Ironfoot, Lord of the Iron Hills: [dwarven] Dwarf ALLY, laid only in a dead pile here
					put("dain", "32_2");

					// The Shadow player's discard pile: a Shadow card of each kind, Smaug, and a Free Peoples card
					// Yazneg, Orkish Assassin: [gundabad] Orc minion
					put("yazneg", "30_41");
					// Warg: [gundabad] possession, a mount
					put("warg", "31_27");
					// Smaug's Awakening: [smaug] Shadow event, a title that is not "Smaug"
					put("awakening", "32_52");
					// Hatred Rekindled: [gundabad] condition
					put("hatred", "30_34");
					// Smaug, The Golden: the Smaug the text excepts
					put("smaug", "30_66");
					// Bofur, Encouraging Fellow: [dwarven] follower, a Free Peoples card
					put("bofur", "30_6");
					// Troll Campfire: [troll] condition with a Shadow ability; another condition of the same player
					put("campfire", "31_59");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void NotatHomeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Not at Home
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: If there is a [dwarven] companion in the dead pile, each [dwarven] companion comes into play
		 * 	exhausted.
		 * 	<b>Regroup:</b> Place a Shadow card (except Smaug) from your discard pile on top of your draw deck. Discard
		 * 	this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("home");

		assertEquals("Not at Home", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithADwarvenCompanionInTheDeadPileEachDwarvenCompanionComesIntoPlayExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var home = scn.GetShadowCard("home");
		var balin = scn.GetFreepsCard("balin");
		var thorin = scn.GetFreepsCard("thorin");
		var gloin = scn.GetFreepsCard("gloin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var army = scn.GetFreepsCard("army");
		scn.MoveCardsToHand(thorin, gloin, gandalf, army);

		scn.StartGame();
		scn.MoveCardsToSupportArea(home);
		scn.MoveCardsToDeadPile(balin);
		assertInZone(Zone.SUPPORT, home);
		assertInZone(Zone.DEAD, balin);
		scn.FreepsRefreshDecision();

		// Vitality 4 and 3: each placed with 1 vitality left.
		scn.FreepsPlayCard(thorin);
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertTrue(scn.IsExhausted(thorin));
		scn.FreepsPlayCard(gloin);
		assertEquals(2, scn.GetWoundsOn(gloin));
		assertTrue(scn.IsExhausted(gloin));

		// A companion of another culture, and a [dwarven] ally, come in fresh.
		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(army);
		assertInZone(Zone.SUPPORT, army);
		assertEquals(0, scn.GetWoundsOn(army));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NoDwarvenCompanionInTheFreePeoplesDeadPileLeavesThemFresh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var home = scn.GetShadowCard("home");
		var balin = scn.GetFreepsCard("balin");
		var gandalf2 = scn.GetFreepsCard("gandalf2");
		var dain = scn.GetFreepsCard("dain");
		var shadowBalin = scn.GetShadowCard("balin");
		var thorin = scn.GetFreepsCard("thorin");
		var gloin = scn.GetFreepsCard("gloin");
		scn.MoveCardsToHand(thorin, gloin);

		scn.StartGame();
		scn.MoveCardsToSupportArea(home);
		// A [gandalf] companion and a [dwarven] ally in his dead pile, and a [dwarven] companion in the Shadow
		// player's: "the dead pile" is the Free Peoples player's (Q-0131).
		scn.MoveCardsToDeadPile(gandalf2, dain, shadowBalin);
		assertInZone(Zone.DEAD, gandalf2, dain, shadowBalin);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(thorin);
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertEquals(0, scn.GetWoundsOn(thorin));

		// Then a [dwarven] companion of his own is placed in his dead pile.
		scn.MoveCardsToDeadPile(balin);
		assertInZone(Zone.DEAD, balin);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(gloin);
		assertEquals(2, scn.GetWoundsOn(gloin));
		// Thorin, already in play, is not touched.
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupPlacesAShadowCardOtherThanSmaugOnTopOfTheDeckAndDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var home = scn.GetShadowCard("home");
		var campfire = scn.GetShadowCard("campfire");
		var yazneg = scn.GetShadowCard("yazneg");
		var warg = scn.GetShadowCard("warg");
		var awakening = scn.GetShadowCard("awakening");
		var hatred = scn.GetShadowCard("hatred");
		var smaug = scn.GetShadowCard("smaug");
		var bofur = scn.GetShadowCard("bofur");

		scn.StartGame();
		scn.MoveCardsToSupportArea(home, campfire);
		scn.MoveCardsToDiscard(yazneg, warg, awakening, hatred, smaug, bofur);
		assertInZone(Zone.SUPPORT, home, campfire);
		assertInDiscard(yazneg, warg, awakening, hatred, smaug, bofur);
		assertNotSame(awakening, scn.GetShadowTopOfDeck());
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(home));
		scn.ShadowUseCardAction(home);

		// A minion, a possession, an event and a condition; not Smaug, nor a Free Peoples card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(yazneg, warg, awakening, hatred));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(smaug, bofur));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(awakening);

		assertSame(awakening, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, awakening);
		assertInDiscard(yazneg, warg, hatred, smaug, bofur);
		// Then the condition goes, and another condition of his stays.
		assertInDiscard(home);
		assertInZone(Zone.SUPPORT, campfire);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoShadowCardButSmaugToPlaceItIsStillUsedAndDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var home = scn.GetShadowCard("home");
		var smaug = scn.GetShadowCard("smaug");
		var bofur = scn.GetShadowCard("bofur");

		scn.StartGame();
		scn.MoveCardsToSupportArea(home);
		scn.MoveCardsToDiscard(smaug, bofur);
		assertInZone(Zone.SUPPORT, home);
		assertInDiscard(smaug, bofur);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Its own "Discard this condition" is something done, so it is offered.
		assertTrue(scn.ShadowActionAvailable(home));
		scn.ShadowUseCardAction(home);

		assertInDiscard(smaug, bofur, home);
		assertNotSame(smaug, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var home = scn.GetShadowCard("home");
		var yazneg = scn.GetShadowCard("yazneg");
		var awakening = scn.GetShadowCard("awakening");

		scn.StartGame();
		scn.MoveCardsToSupportArea(home);
		scn.MoveCardsToDiscard(awakening);
		assertInZone(Zone.SUPPORT, home);
		assertInDiscard(awakening);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(home));
		assertInZone(Zone.SUPPORT, home);
	}
}
