package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_139_Tests
{
	// The card's own block: the Shadow player lays each next site, and any site laid as site 3 is a sanctuary.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man, vitality 4
					put("aragorn2", "1_89");      // a second copy, held in hand to be discarded to heal the first
					put("guard", "5_83");         // Household Guard: a [rohan] Man companion, vitality 3; valiant only
					// Guma, Plains Farmer, a [rohan] Man ally of vitality 2: "Fellowship: Exert Guma to heal a
					// [rohan] Man."
					put("guma", "4_277");
					put("sam", "1_311");          // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden."
					// Gondor Will See It Done: "Response: If a companion is about to take a wound, exert a
					// [gondor] companion to prevent that wound."
					put("seeitdone", "2_34");
					// Defend It and Hope: "Maneuver: Exert a [gondor] Man to wound a minion once (or twice if
					// that minion is an Uruk-hai)."
					put("defendit", "4_115");

					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					// Goblin Armory, a [moria] support-area condition: "Response: If a [moria] Orc is about to
					// take a wound, discard this condition to prevent that wound."
					put("armory", "1_173");
					put("marksman", "1_176");     // Goblin Marksman: archer, and nothing else
					put("shaman", "1_152");       // Uruk Shaman, vitality 2: "Maneuver: Remove (2) to heal an Uruk-hai."
					put("savage", "1_151");       // Uruk Savage: an Uruk-hai of vitality 3, damage +1 only
					// Gollum, Dark as Darkness: "Shadow: Remove a burden to add (1)." and, as the ability that
					// stays on offer beside it, "Shadow: Exert Gollum twice to exert the Ring-bearer."
					put("gollum", "9_28");
				}},
				new HashMap<>()
				{{
					put("site1", "18_138");       // Sirannon Ruins: River, and no text
					put("tomb", "18_139");
					put("glade", "11_239");       // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					// Never laid in these tests, and never named.
					put("site4", "11_236");
					put("site5", "11_238");
					put("site6", "11_238");
					put("site7", "11_238");
					put("site8", "11_238");
					put("site9", "11_238");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void StewardsTombStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Steward's Tomb
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: Wounds cannot be prevented or healed. Burdens cannot be removed.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("tomb");

		assertEquals("Steward's Tomb", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AsSiteOneTheFellowshipHealsNoWoundAndRemovesNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var aragorn2 = scn.GetFreepsCard("aragorn2");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(aragorn2);

		var tomb = scn.GetFreepsSite("tomb");

		scn.StartGame(tomb);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 2);
		scn.AddBurdens(2);
		scn.FreepsRefreshDecision();

		assertEquals(tomb, scn.GetCurrentSite());
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetBurdens());

		// Guma pays, and heals neither the companion nor himself.
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.FreepsActionAvailable(guma));
		scn.FreepsUseCardAction(guma);
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A copy of Aragorn discarded from hand heals nothing either.
		assertTrue(scn.FreepsHasDiscardToHealAvailable());
		scn.FreepsDiscardToHeal(aragorn2);
		assertInZone(Zone.DISCARD, aragorn2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Sam pays, and both burdens stay.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AsSiteTwoShadowCardsHealNoWoundAndCannotRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var shaman = scn.GetShadowCard("shaman");
		var savage = scn.GetShadowCard("savage");

		var tomb = scn.GetShadowSite("tomb");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddBurdens(2);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(tomb);

		scn.MoveMinionsToTable(gollum, shaman, savage);
		scn.AddWoundsToChar(shaman, 1);
		scn.AddWoundsToChar(savage, 2);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertEquals(tomb, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());

		// A burden that cannot be removed cannot pay for Gollum's twilight; his other Shadow ability is on offer.
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(gollum, "Exert the Ring-bearer"));
		assertFalse(scn.ShadowActionAvailable(gollum, "Add (1)"));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The Uruk Shaman pays (2), and heals neither Uruk-hai.
		assertEquals(5, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(shaman));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowActionAvailable(shaman));
		scn.ShadowUseCardAction(shaman);
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(shaman));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AsSiteTwoNeitherPlayersResponsePreventsAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var seeitdone = scn.GetFreepsCard("seeitdone");
		var defendit = scn.GetFreepsCard("defendit");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToHand(seeitdone, defendit);

		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(armory);

		var tomb = scn.GetShadowSite("tomb");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(tomb);
		scn.MoveMinionsToTable(troop);
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(tomb, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Defend It and Hope wounds the Goblin Patrol Troop; the Goblin Armory is discarded and prevents nothing.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPlayCard(defendit);
		scn.FreepsChooseCard(troop);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armory));
		scn.ShadowChooseOptionalTrigger(armory);
		assertInZone(Zone.DISCARD, armory);
		assertEquals(1, scn.GetWoundsOn(troop));
		// Aragorn exerted for the event.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The Goblin Marksman's archery wound goes to the Household Guard; Gondor Will See It Done exerts
		// Aragorn and prevents nothing.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsPlayAvailable(seeitdone));
		scn.FreepsPlayCard(seeitdone);
		assertInZone(Zone.DISCARD, seeitdone);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AsSiteThreeASanctuaryTheTombLetsTheSanctuaryHealNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		var tomb = scn.GetShadowSite("tomb");

		// Two moves in the first turn, to Fangorn Glade and on to the Tomb as site 3, a sanctuary by its number;
		// then the other player's turn, and the first player's second turn begins at the Tomb.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("glade"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(tomb);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(guard));
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals(tomb, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());

		// The sanctuary's own healing heals no wound: with no companion it could heal, it is not asked, and the
		// turn opens on the fellowship actions.
		assertFalse(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheTombTheFellowshipHealsAndRemovesABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var aragorn2 = scn.GetFreepsCard("aragorn2");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(guma);

		var tomb = scn.GetFreepsSite("tomb");
		var glade = scn.GetShadowSite("glade");

		// The Tomb is site 1, and the fellowship moves from it to Fangorn Glade, site 2 and no sanctuary; the
		// first player's second turn begins there.
		scn.StartGame(tomb);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 2);
		scn.AddBurdens(2);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToHand(aragorn2);
		scn.FreepsRefreshDecision();

		assertEquals(tomb, scn.GetSite(1));
		assertEquals(glade, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Guma exerts, and heals one of the two wounded [rohan] Men.
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(2, scn.GetWoundsOn(guard));
		scn.FreepsUseCardAction(guma);
		assertEquals(1, scn.GetWoundsOn(guma));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(guma));

		// A copy of Aragorn discarded from hand heals him.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsDiscardToHeal(aragorn2);
		assertInZone(Zone.DISCARD, aragorn2);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Sam removes a burden.
		assertEquals(2, scn.GetBurdens());
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheTombEachPlayersResponsePreventsAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var seeitdone = scn.GetFreepsCard("seeitdone");
		var defendit = scn.GetFreepsCard("defendit");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToHand(seeitdone, defendit);

		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(armory);

		var tomb = scn.GetFreepsSite("tomb");
		var glade = scn.GetShadowSite("glade");

		// The Tomb is site 1, and the fellowship moves from it to Fangorn Glade.
		scn.StartGame(tomb);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.MoveMinionsToTable(troop);
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(tomb, scn.GetSite(1));
		assertEquals(glade, scn.GetCurrentSite());

		// The Goblin Armory prevents the wound of Defend It and Hope.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPlayCard(defendit);
		scn.FreepsChooseCard(troop);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armory));
		scn.ShadowChooseOptionalTrigger(armory);
		assertInZone(Zone.DISCARD, armory);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Gondor Will See It Done prevents the archery wound.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsPlayAvailable(seeitdone));
		scn.FreepsPlayCard(seeitdone);
		assertInZone(Zone.DISCARD, seeitdone);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheTombShadowCardsHealAWoundAndRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var shaman = scn.GetShadowCard("shaman");
		var savage = scn.GetShadowCard("savage");

		var tomb = scn.GetFreepsSite("tomb");
		var glade = scn.GetShadowSite("glade");

		// The Tomb is site 1, and the fellowship moves from it to Fangorn Glade: site 2, and no sanctuary.
		scn.StartGame(tomb);
		scn.AddBurdens(2);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);

		scn.MoveMinionsToTable(gollum, shaman, savage);
		scn.AddWoundsToChar(shaman, 1);
		scn.AddWoundsToChar(savage, 2);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertEquals(tomb, scn.GetSite(1));
		assertEquals(glade, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(gollum, "Add (1)"));
		scn.ShadowUseCardAction(gollum, "Add (1)");
		assertEquals(1, scn.GetBurdens());
		assertEquals(6, scn.GetTwilight());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(shaman));
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.ShadowUseCardAction(shaman);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(shaman, savage));
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(shaman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
