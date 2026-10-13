package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_119_Tests
{

	// The King-block path of the rig, with Steward's Tomb as its site 5.
	protected VirtualTableScenario GetTombScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man, vitality 4
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
					// Lost to the Goblins, a Shadow event: "Response: If the Ring-bearer exerts or takes a wound,
					// ..."  Never played: it is here to be offered when he takes a wound.
					put("lost", "1_189");

					put("aragorn2", "1_89");      // a second copy, held in hand to be discarded to heal the first
					put("guard", "5_83");         // Household Guard: a [rohan] Man companion, vitality 3; valiant only
					// Guma, Plains Farmer, a [rohan] Man ally of vitality 2 (home 4T): "Fellowship: Exert Guma to
					// heal a [rohan] Man."  His other sentence is about his home site, which nobody controls.
					put("guma", "4_277");
					put("sam", "1_311");          // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden."
					put("shaman", "1_152");       // Uruk Shaman, vitality 2: "Maneuver: Remove (2) to heal an Uruk-hai."
					put("savage", "1_151");       // Uruk Savage: an Uruk-hai of vitality 3, damage +1 only
					// Gollum, Dark as Darkness: "Shadow: Remove a burden to add (1)." and, as the ability that
					// stays on offer beside it, "Shadow: Exert Gollum twice to exert the Ring-bearer."
					put("gollum", "9_28");
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "10_119");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StewardsTombStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Steward's Tomb
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 5K
		 * Shadow Number: 8
		 * Direction: Left
		 * Game Text: Wounds cannot be prevented or healed. Burdens cannot be removed.
		 */

		var scn = GetTombScenario();

		var card = scn.GetFreepsSite("site5");

		assertEquals("Steward's Tomb", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtTheTombTheFellowshipHealsNoWoundAndRemovesNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTombScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var aragorn2 = scn.GetFreepsCard("aragorn2");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(5);
		// The turns played on the way drew the deck into the hand: the board is set out now.
		scn.SetFreepsHand(aragorn2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 2);
		scn.AddBurdens(2);
		scn.FreepsRefreshDecision();

		assertEquals("Steward's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(5, scn.GetCurrentSiteNumber());

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
	public void AtTheTombShadowCardsHealNoWoundAndCannotRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTombScenario();

		var gollum = scn.GetShadowCard("gollum");
		var shaman = scn.GetShadowCard("shaman");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AddBurdens(2);
		scn.FreepsPass();

		scn.MoveMinionsToTable(gollum, shaman, savage);
		scn.AddWoundsToChar(shaman, 1);
		scn.AddWoundsToChar(savage, 2);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertEquals("Steward's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(5, scn.GetCurrentSiteNumber());

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
	public void OnceTheFellowshipHasMovedOnFromTheTombTheFellowshipHealsAndRemovesABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTombScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var aragorn2 = scn.GetFreepsCard("aragorn2");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(guma);

		// The turn begins at site 6, with the Tomb behind the fellowship as site 5.  Nobody is wounded as it
		// begins, so the sanctuary heals nothing; the wounds are put on afterwards.
		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetFreepsHand(aragorn2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 2);
		scn.AddBurdens(2);
		scn.FreepsRefreshDecision();

		assertEquals("Steward's Tomb", scn.GetSite(5).getBlueprint().getTitle());
		assertEquals(6, scn.GetCurrentSiteNumber());

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
	public void OnceTheFellowshipHasMovedOnFromTheTombShadowCardsHealAWoundAndRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTombScenario();

		var gollum = scn.GetShadowCard("gollum");
		var shaman = scn.GetShadowCard("shaman");
		var savage = scn.GetShadowCard("savage");

		// The turn begins at the Tomb, and the fellowship moves on from it to site 6.
		scn.StartGame();
		scn.SkipToSite(5);
		scn.AddBurdens(2);
		scn.FreepsPass();

		scn.MoveMinionsToTable(gollum, shaman, savage);
		scn.AddWoundsToChar(shaman, 1);
		scn.AddWoundsToChar(savage, 2);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertEquals("Steward's Tomb", scn.GetSite(5).getBlueprint().getTitle());
		assertEquals(6, scn.GetCurrentSiteNumber());

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

	@Test
	public void OnceTheFellowshipHasMovedOnFromTheTombEachPlayersResponsePreventsAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTombScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var seeitdone = scn.GetFreepsCard("seeitdone");
		var defendit = scn.GetFreepsCard("defendit");

		var troop = scn.GetShadowCard("troop");
		var armory = scn.GetShadowCard("armory");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable(aragorn);
		scn.SetFreepsHand(seeitdone, defendit);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop, marksman);
		scn.MoveCardsToSupportArea(armory);
		scn.SetShadowHand();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals("Steward's Tomb", scn.GetSite(5).getBlueprint().getTitle());
		assertEquals(6, scn.GetCurrentSiteNumber());

		// The Goblin Armory prevents the wound of Defend It and Hope.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPlayCard(defendit);
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armory));
		scn.ShadowChooseOptionalTrigger(armory);
		assertInZone(Zone.DISCARD, armory);
		assertEquals(0, scn.GetWoundsOn(troop));

		// Gondor Will See It Done prevents the Goblin Marksman's archery wound on Frodo.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsPlayAvailable(seeitdone));
		scn.FreepsPlayCard(seeitdone);
		assertInDiscard(seeitdone);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AWoundCannotBePreventedHereAndWhatIsPaidToPreventItIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTombScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var seeitdone = scn.GetFreepsCard("seeitdone");
		var defendit = scn.GetFreepsCard("defendit");

		var troop = scn.GetShadowCard("troop");
		var armory = scn.GetShadowCard("armory");
		var marksman = scn.GetShadowCard("marksman");
		var lost = scn.GetShadowCard("lost");

		scn.StartGame();
		scn.SkipToSite(4);
		// The turns played on the way drew both decks into the hands: the board is set out now.
		scn.MoveCompanionsToTable(aragorn);
		scn.SetFreepsHand(seeitdone, defendit);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop, marksman);
		scn.MoveCardsToSupportArea(armory);
		scn.SetShadowHand(lost);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals("Steward's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());

		// Defend It and Hope wounds the Goblin Patrol Troop, Aragorn exerting for it.  The Goblin Armory's
		// response is offered and taken: the condition is discarded, and the wound lands all the same.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(defendit);
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armory));
		scn.ShadowChooseOptionalTrigger(armory);
		assertInZone(Zone.DISCARD, armory);
		assertEquals(1, scn.GetWoundsOn(troop));

		// The Goblin Marksman's archery wound goes to Frodo.  Gondor Will See It Done is offered and played:
		// Aragorn exerts again, and the wound lands all the same.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsPlayAvailable(seeitdone));
		scn.FreepsPlayCard(seeitdone);
		assertInDiscard(seeitdone);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));

		// It was a wound taken like any other: the event that answers a wound the Ring-bearer takes is offered.
		assertTrue(scn.ShadowPlayAvailable(lost));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
