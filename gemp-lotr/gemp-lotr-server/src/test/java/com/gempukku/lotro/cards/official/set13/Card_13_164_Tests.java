package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_164_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fearless", "13_164");
					put("fearless2", "13_164");   // non-unique; a second copy holds the token being reinforced
					put("outrider", "13_172");    // Uruk Outrider: [uruk-hai]-culture minion, passive only; satisfies the play requirement
					put("bowmen", "1_264");       // Orc Bowmen: Shadow support condition, inert without [sauron] Orcs
					put("lordOfMoria", "1_21");   // Free Peoples support condition, inert without Dwarves
					put("toby", "1_305");         // Old Toby: a possession, must NOT be a valid discard target
					put("speak", "2_26");         // Speak "Friend" and Enter
					put("gandalf", "1_72");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	protected VirtualTableScenario GetBattlegroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fearless", "13_164");
					put("fearless2", "13_164");
					put("outrider", "13_172");
				}},
				new HashMap<>()
				{{
					put("site1", "11_258"); // Slag Mounds: Battleground, only reacts to companions being killed
					put("site2", "13_185");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void FearlessApproachStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Fearless Approach
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot an [uruk-hai] minion.<br>When you play this, if you spot a battleground site on the adventure path, reinforce 3 [uruk-hai] tokens.<br>Each time the Free Peoples player plays the fellowship's next site, you may discard a condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fearless");

		assertEquals("Fearless Approach", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayingWithBattlegroundOnAdventurePathReinforces3UrukHaiTokens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetBattlegroundScenario();

		var fearless = scn.GetShadowCard("fearless");
		var fearless2 = scn.GetShadowCard("fearless2");
		var outrider = scn.GetShadowCard("outrider");
		scn.MoveCardsToHand(fearless);
		scn.MoveMinionsToTable(outrider);

		var site1 = scn.GetFreepsSite("site1");
		scn.StartGame(site1);
		// Cheated in after StartGame: the "next site played" trigger would otherwise fire on site 1 being placed
		scn.MoveCardsToSupportArea(fearless2);
		scn.AddTokensToCard(fearless2, 1);

		// Move to site 2 so we reach a Shadow phase; site 1 (Slag Mounds) stays on the path
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseAnyCard();
		scn.SetTwilight(10);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(fearless2));
		assertTrue(scn.ShadowPlayAvailable(fearless));

		scn.ShadowPlayCard(fearless);

		// fearless2 is the only [uruk-hai] card bearing a token, so each of the 3 reinforcements auto-targets it
		assertEquals(4, scn.GetCultureTokensOn(fearless2));
		assertEquals(0, scn.GetCultureTokensOn(fearless));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingWithoutBattlegroundOnAdventurePathDoesNotReinforce() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var fearless2 = scn.GetShadowCard("fearless2");
		var outrider = scn.GetShadowCard("outrider");
		scn.MoveCardsToHand(fearless);
		scn.MoveMinionsToTable(outrider);

		var site1 = scn.GetFreepsSite("site1");
		scn.StartGame(site1);
		// Cheated in after StartGame: the "next site played" trigger would otherwise fire on site 1 being placed
		scn.MoveCardsToSupportArea(fearless2);
		scn.AddTokensToCard(fearless2, 1);

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseAnyCard();
		scn.SetTwilight(10);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(fearless2));

		scn.ShadowPlayCard(fearless);

		assertEquals(1, scn.GetCultureTokensOn(fearless2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreePeoplesPlayingNextSiteLetsShadowDiscardACondition() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1005: the third sentence was missing from the definition entirely.
		var scn = GetScenario();

		var speak = scn.GetFreepsCard("speak");
		var gandalf = scn.GetFreepsCard("gandalf");
		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(lordOfMoria, toby);

		var fearless = scn.GetShadowCard("fearless");
		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCardsToSupportArea(bowmen);

		var site1 = scn.GetFreepsSite("site1");
		var site2 = scn.GetFreepsSite("site2");
		scn.StartGame(site1);
		// Cheated in after StartGame: the trigger would otherwise fire on site 1 being placed
		scn.MoveCardsToSupportArea(fearless);

		assertEquals(Zone.ADVENTURE_DECK, site2.getZone());
		assertEquals(Zone.SUPPORT, lordOfMoria.getZone());

		scn.FreepsPlayCard(speak);
		scn.FreepsChooseCardBPFromSelection(site2);
		assertEquals(Zone.ADVENTURE_PATH, site2.getZone());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Any condition in play is a legal target (including Fearless Approach itself); possessions are not
		assertTrue(scn.ShadowHasCardChoiceAvailable(lordOfMoria, bowmen, fearless));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(toby));
		scn.ShadowChooseCard(lordOfMoria);

		assertEquals(Zone.DISCARD, lordOfMoria.getZone());
		assertEquals(Zone.SUPPORT, bowmen.getZone());
		assertEquals(Zone.SUPPORT, toby.getZone());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FreePeoplesPlayingNextSiteTriggerIsOptional() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetFreepsCard("speak");
		var gandalf = scn.GetFreepsCard("gandalf");
		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(lordOfMoria);

		var fearless = scn.GetShadowCard("fearless");

		var site1 = scn.GetFreepsSite("site1");
		var site2 = scn.GetFreepsSite("site2");
		scn.StartGame(site1);
		scn.MoveCardsToSupportArea(fearless);

		scn.FreepsPlayCard(speak);
		scn.FreepsChooseCardBPFromSelection(site2);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(Zone.SUPPORT, lordOfMoria.getZone());
		assertEquals(Zone.SUPPORT, fearless.getZone());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowPlayingNextSiteDuringMovementDoesNotTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		scn.MoveCardsToSupportArea(lordOfMoria);

		var fearless = scn.GetShadowCard("fearless");

		var site1 = scn.GetFreepsSite("site1");
		scn.StartGame(site1);
		scn.MoveCardsToSupportArea(fearless);

		// Normal movement: the Shadow player plays the next site, which is not "the Free Peoples player plays"
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseAnyCard();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(Zone.SUPPORT, lordOfMoria.getZone());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
