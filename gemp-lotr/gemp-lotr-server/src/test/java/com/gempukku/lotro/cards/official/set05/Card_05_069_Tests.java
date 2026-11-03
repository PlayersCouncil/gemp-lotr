package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wolves", "5_69");

					// Isengard Shaman: an [isengard] Orc, vitality 2; Response: if an [isengard] Orc is about to take a
					// wound, remove (2) to prevent it (an exertion is no wound)
					put("shaman", "3_59");
					// Isengard Worker: an [isengard] Orc, vitality 3, with a regroup ability of its own, never used
					put("worker", "3_62");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, no Orc; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text, vitality 3

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability only
					// Barliman Butterbur: a [gandalf] Man ally, vitality 2; a fellowship ability only
					put("barliman", "1_70");
				}},
				// Site 2, Ettenmoors, is a plains.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPlainsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wolves", "5_69");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");      // Eastemnet Gullies: no terrain; a fellowship ability only
					put("site2", "4_331");      // Eastfold: plains; a Shadow ability that wants 3 [isengard] trackers
					put("site3", "4_342");      // Westemnet Plains: plains and a sanctuary; a bar on [dunland] Men only
					put("site4", "4_344");      // Westemnet Hills: plains; heals mounted characters moving there
					put("site5", "4_347");      // Deep of Helm: plains and a battleground; a discount for Uruk-hai only
					put("site6", "4_352");      // Caves of Aglarond: underground, a sanctuary; no plains
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WolvesofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Wolves of Isengard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype:
		 * Game Text: Plays on a plains site you control.<br><b>Regroup:</b> Exert an [isengard] Orc to make the Free Peoples player wound a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wolves");

		assertEquals("Wolves of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAPlainsSiteItsPlayerControlsAndOnNoOtherSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlainsScenario();

		var wolves = scn.GetShadowCard("wolves");

		scn.StartGame();
		// Both markers at site 5: sites 1 to 4 are behind them.
		scn.SkipToSite(5);
		scn.MoveCardsToHand(wolves);
		assertInHand(wolves);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		var site5 = scn.GetSite(5);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		// The other player controls site 4, from an earlier turn of his as the Shadow player.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site3));
		assertTrue(scn.FreepsControls(site4));

		scn.FreepsPass();
		var site6 = scn.GetSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(wolves));

		scn.ShadowPlayCard(wolves);
		// Two plains sites he controls, one of them a sanctuary.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site2, site3));
		// A site he controls that is no plains, the other player's plains, a plains on the path nobody controls, and
		// the site the fellowship is at.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site1, site4, site5, site6));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(site3);

		assertAttachedTo(wolves, site3);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsAnIsengardOrcToMakeTheFreePeoplesPlayerWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(barliman);

		var wolves = scn.GetShadowCard("wolves");
		var shaman = scn.GetShadowCard("shaman");
		var worker = scn.GetShadowCard("worker");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Both markers at site 3: sites 1 and 2 are behind them, and site 2 is Ettenmoors, a plains.
		scn.SkipToSite(3);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site2));
		scn.AttachCardsTo(site2, wolves);
		assertAttachedTo(wolves, site2);
		// Gimli, vitality 3, is exhausted: a wound kills him.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(shaman, worker, savage, troop);

		// The maneuver phase at site 4, the phase before the regroup phase of this turn: not offered.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wolves));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(wolves));

		scn.ShadowUseCardAction(wolves);
		// Either [isengard] Orc; not an [isengard] Uruk-hai, not an Orc of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(shaman, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, troop));
		scn.ShadowChooseCard(shaman);
		assertEquals(1, scn.GetWoundsOn(shaman));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));

		// The Free Peoples player chooses the companion: any companion, exhausted or not, and no ally or minion.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, worker));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// A wound and not an exertion: the exhausted Gimli is killed.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileTheOnlyIsengardOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var wolves = scn.GetShadowCard("wolves");
		var shaman = scn.GetShadowCard("shaman");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site2));
		scn.AttachCardsTo(site2, wolves);
		assertAttachedTo(wolves, site2);
		scn.MoveMinionsToTable(shaman, savage, troop);
		// The Shaman, vitality 2, is exhausted; the Uruk-hai and the [moria] Orc beside him could exert.
		scn.AddWoundsToChar(shaman, 1);
		assertEquals(1, scn.GetWoundsOn(shaman));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(shaman));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wolves));
	}
}
