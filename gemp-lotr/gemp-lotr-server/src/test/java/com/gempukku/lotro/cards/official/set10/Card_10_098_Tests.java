package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hall", "10_98");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc minion
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai minion; Damage +1 only
					put("towers", "7_281");     // Great Siege-towers: [sauron] condition
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion; Damage +1 only
					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] Orc minion; a Shadow ability used only on purpose
					put("banner", "6_98");      // Banner of the Eye: [sauron] hand weapon, cost 0, "Bearer must be a [sauron] Orc"

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession, a hand weapon a Dwarf bears
					put("axe2", "1_9");
					put("cart", "1_73");        // Gandalf's Cart: [gandalf] possession to the support area; a fellowship ability used only on purpose
					put("shards", "3_44");      // The Shards of Narsil: [gondor] artifact to the support area; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion to play
					put("delving", "1_6");      // Delving: Fellowship event, "Exert a Dwarf companion to draw 3 cards"
					put("baruk", "5_5");        // Baruk Khazad: Maneuver event, "Exert a Dwarf to wound a minion"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RuinousHallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Ruinous Hall
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If the Free Peoples player plays a possession, discard a [sauron] minion from hand to prevent him or her from playing any more cards until the end of this phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hall");

		assertEquals("Ruinous Hall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void APossessionPlayedLetsTheShadowPlayerDiscardASauronMinionToStopTheFreePeoplesPlayerPlayingCardsThisPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var cart = scn.GetFreepsCard("cart");
		var aragorn = scn.GetFreepsCard("aragorn");
		var delving = scn.GetFreepsCard("delving");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(axe, axe2, cart, aragorn, delving, baruk);

		var hall = scn.GetShadowCard("hall");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var towers = scn.GetShadowCard("towers");
		var savage = scn.GetShadowCard("savage");
		var taskmaster = scn.GetShadowCard("taskmaster");
		scn.MoveCardsToHand(hall, chieftain, guard, towers, savage);
		assertInHand(hall, chieftain, guard, towers, savage);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(axe2));
		assertTrue(scn.FreepsPlayAvailable(aragorn));
		assertTrue(scn.FreepsPlayAvailable(delving));

		// A possession borne by a companion: offered, and let go.
		scn.FreepsPlayCard(axe);
		assertAttachedTo(axe, gimli);
		assertTrue(scn.ShadowPlayAvailable(hall));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A possession of another culture, to the support area: offered again, and played.
		scn.FreepsPlayCard(cart);
		assertInZone(Zone.SUPPORT, cart);
		assertTrue(scn.ShadowPlayAvailable(hall));
		scn.ShadowPlayCard(hall);

		// [sauron] minions of two races in hand; not the [sauron] condition, nor the [isengard] minion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(towers, savage));
		scn.ShadowChooseCard(chieftain);
		assertInDiscard(chieftain, hall);
		assertInHand(guard, towers, savage);
		assertInZone(Zone.SUPPORT, cart);
		assertAttachedTo(axe, gimli);

		// No card of any type can be played for the rest of the fellowship phase.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(axe2));
		assertFalse(scn.FreepsPlayAvailable(aragorn));
		assertFalse(scn.FreepsPlayAvailable(delving));

		// In the maneuver phase the Free Peoples player plays cards again.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(baruk));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(taskmaster));
		scn.FreepsPlayCard(baruk);
		assertInDiscard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheFreePeoplesPlayerPlaysAnArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var shards = scn.GetFreepsCard("shards");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(shards, axe);

		var hall = scn.GetShadowCard("hall");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(hall, chieftain);
		assertInHand(hall, chieftain);

		scn.StartGame();
		scn.FreepsPlayCard(shards);
		assertInZone(Zone.SUPPORT, shards);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The same board offers it for a possession.
		scn.FreepsPlayCard(axe);
		assertTrue(scn.ShadowPlayAvailable(hall));
		assertInHand(hall);
		assertTrue(scn.ShadowDecisionAvailable("Optional responses"));
	}

	@Test
	public void NotOfferedWhenTheShadowPlayerPlaysAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hall = scn.GetShadowCard("hall");
		var chieftain = scn.GetShadowCard("chieftain");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCardsToHand(hall, chieftain, banner);
		assertInHand(hall, chieftain, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(banner);
		assertAttachedTo(banner, taskmaster);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hall));
		assertInHand(hall);
	}

	@Test
	public void NotOfferedWithNoSauronMinionInHandToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(axe);

		var hall = scn.GetShadowCard("hall");
		var towers = scn.GetShadowCard("towers");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hall, towers, savage);
		assertInHand(hall, towers, savage);

		scn.StartGame();
		scn.FreepsPlayCard(axe);
		assertAttachedTo(axe, gimli);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInHand(hall, towers, savage);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
