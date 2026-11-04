package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dispatcher", "17_42");
					put("dispatcher2", "17_42");

					// Squad of Haradrim, Easterling Runner: [men] Men of two titles; their abilities are not
					// offered here (the Runner's wants a culture token to remove)
					put("squad", "11_99");
					put("runner", "13_90");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is no Man (a Hobbit)
					put("ted", "18_75");
					// Dunlending Brigand: a [dunland] Man with no text
					put("brigand", "4_10");

					// Henchman's Dagger: a Shadow possession, borne (on a [men] minion); no text in play here
					put("dagger", "18_69");
					// Old Toby: a Free Peoples possession in the support area; its draw answers only its own play
					put("toby", "1_305");
					// The Shards of Narsil: a Free Peoples ARTIFACT in the support area, the item that is no
					// possession; its abilities are Fellowship actions
					put("shards", "3_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingDispatcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Easterling Dispatcher
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, you may spot 2 other [men] Men to add (1) for each possession you spot.
		*/

		var scn = GetScenario();

		var dispatcher = scn.GetFreepsCard("dispatcher");

		assertEquals("Easterling Dispatcher", dispatcher.getBlueprint().getTitle());
		assertNull(dispatcher.getBlueprint().getSubtitle());
		assertFalse(dispatcher.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, dispatcher.getBlueprint().getSide());
		assertEquals(Culture.MEN, dispatcher.getBlueprint().getCulture());
		assertEquals(CardType.MINION, dispatcher.getBlueprint().getCardType());
		assertEquals(Race.MAN, dispatcher.getBlueprint().getRace());
		assertEquals(3, dispatcher.getBlueprint().getTwilightCost());
		assertEquals(8, dispatcher.getBlueprint().getStrength());
		assertEquals(2, dispatcher.getBlueprint().getVitality());
		assertEquals(4, dispatcher.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithTwoOtherMenMenItAddsOneForEachPossessionThePlayerChoosesToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toby = scn.GetFreepsCard("toby");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCardsToSupportArea(toby, shards);

		var dispatcher = scn.GetShadowCard("dispatcher");
		var squad = scn.GetShadowCard("squad");
		var runner = scn.GetShadowCard("runner");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveCardsToHand(dispatcher);

		scn.StartGame();
		scn.MoveMinionsToTable(squad, runner);
		scn.AttachCardsTo(squad, dagger);
		assertAttachedTo(dagger, squad);
		assertInZone(Zone.SUPPORT, toby, shards);
		assertInHand(dispatcher);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());

		scn.ShadowPlayCard(dispatcher);
		// 3 to play + 2 roaming at site 2
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dispatcher));
		scn.ShadowAcceptOptionalTrigger();

		// Two possessions, a Free Peoples one in a support area and a Shadow one borne; the artifact is no
		// possession.  The number is the player's, from 0, and comes filled in with all of them.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("1");

		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondCopyPlayedAfterTheFirstMakesOnlyItsOwnOfferAndAddsTwoForTwoPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToSupportArea(toby);

		var dispatcher = scn.GetShadowCard("dispatcher");
		var dispatcher2 = scn.GetShadowCard("dispatcher2");
		var squad = scn.GetShadowCard("squad");
		var runner = scn.GetShadowCard("runner");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveCardsToHand(dispatcher, dispatcher2);

		scn.StartGame();
		scn.MoveMinionsToTable(squad, runner);
		scn.AttachCardsTo(squad, dagger);
		assertAttachedTo(dagger, squad);
		assertInZone(Zone.SUPPORT, toby);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());

		scn.ShadowPlayCard(dispatcher);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("1");
		// 13 - 5 to play + 1
		assertEquals(9, scn.GetTwilight());

		// The second Dispatcher is another [men] Man played: the first does not answer it; the second's own
		// trigger is the one offer.
		scn.ShadowPlayCard(dispatcher2);
		// 9 - 5 to play
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dispatcher2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dispatcher));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dispatcher));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyOneOtherMenManBesideAMenHobbitAndADunlandMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToSupportArea(toby);

		var dispatcher = scn.GetShadowCard("dispatcher");
		var squad = scn.GetShadowCard("squad");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveCardsToHand(dispatcher);

		scn.StartGame();
		scn.MoveMinionsToTable(squad, ted, brigand);
		scn.AttachCardsTo(squad, dagger);
		assertAttachedTo(dagger, squad);
		assertInZone(Zone.SUPPORT, toby);
		assertInHand(dispatcher);
		scn.SetTwilight(10);
		scn.FreepsPass();


		// One other [men] Man: Ted is a Hobbit, the Brigand of [dunland], and the Dispatcher is not "other".
		scn.ShadowPlayCard(dispatcher);
		assertInZone(Zone.SHADOW_CHARACTERS, dispatcher);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dispatcher));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoPossessionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		scn.MoveCardsToSupportArea(shards);

		var dispatcher = scn.GetShadowCard("dispatcher");
		var squad = scn.GetShadowCard("squad");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(dispatcher);

		scn.StartGame();
		scn.MoveMinionsToTable(squad, runner);
		assertInZone(Zone.SUPPORT, shards);
		assertInHand(dispatcher);
		scn.SetTwilight(10);
		scn.FreepsPass();


		// Two other [men] Men, and nothing to count but an artifact: a yes would add nothing.
		scn.ShadowPlayCard(dispatcher);
		assertInZone(Zone.SHADOW_CHARACTERS, dispatcher);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dispatcher));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToSupportArea(toby);

		var dispatcher = scn.GetShadowCard("dispatcher");
		var squad = scn.GetShadowCard("squad");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(dispatcher);

		scn.StartGame();
		scn.MoveMinionsToTable(squad, runner);
		assertInZone(Zone.SUPPORT, toby);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());

		scn.ShadowPlayCard(dispatcher);
		// 3 to play + 2 roaming at site 2
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dispatcher));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
