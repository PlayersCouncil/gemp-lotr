package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attack", "17_32");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; his skirmish ability is used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] HOBBIT companion; his skirmish ability is used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ALLY; his maneuver ability is used only on purpose
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession, for Frodo; no text

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; his text answers only a weapon played on him
					put("banner", "6_98");      // Banner of the Eye: [sauron] possession on a [sauron] Orc; its trigger answers only its bearer's win
					put("poison", "8_28");      // Spider Poison: [gollum] possession in the support area; its texts need Shelob or a bearer
					put("stone", "9_47");       // Ithil Stone: a [sauron] ARTIFACT in the support area; its texts act only in the Shadow phase
					put("armory", "1_157");     // Uruk-hai Armory: an [isengard] CONDITION in the support area; nothing while no Uruk-hai is in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NimbleAttackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Nimble Attack
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship or Skirmish
		 * Game Text: Spot 2 [gondor] companions and add a threat to discard a Shadow possession.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attack");

		assertEquals("Nimble Attack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void InASkirmishSpotting2GondorCompanionsAndAddingAThreatDiscardsAShadowPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(attack);

		var troop = scn.GetShadowCard("troop");
		var chieftain = scn.GetShadowCard("chieftain");
		var banner = scn.GetShadowCard("banner");
		var poison = scn.GetShadowCard("poison");
		var stone = scn.GetShadowCard("stone");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(poison, stone, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, chieftain);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(sword, frodo);
		assertAttachedTo(banner, chieftain);
		assertInZone(Zone.SUPPORT, poison, stone, armory);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		// The Chieftain is left unassigned, and stands by with his Banner.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(attack));
		scn.FreepsPlayCard(attack);

		assertEquals(1, scn.GetThreats());
		// A Shadow possession borne and one in a support area, of two cultures; not a Shadow artifact, a Shadow
		// condition, or a Free Peoples possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(banner, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stone, armory, sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(banner);

		assertInZone(Zone.DISCARD, banner, attack);
		assertInZone(Zone.SUPPORT, poison, stone, armory);
		assertAttachedTo(sword, frodo);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheFellowshipPhaseItDiscardsAShadowPossessionInASupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.MoveCardsToHand(attack);

		var poison = scn.GetShadowCard("poison");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(poison, armory);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, poison, armory);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(attack));

		scn.FreepsPlayCard(attack);

		// The Spider Poison is the only Shadow possession, and goes unasked.
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DISCARD, poison, attack);
		assertInZone(Zone.SUPPORT, armory);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntil2GondorCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(attack);

		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, poison);

		// One [gondor] companion, beside a [gondor] ally and Frodo, a companion of another culture.
		assertInHand(attack);
		assertFalse(scn.FreepsPlayAvailable(attack));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCompanionsToTable(pippin);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(attack));
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.MoveCardsToHand(attack);

		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, poison);
		// Three companions, three threats: no more can be added.
		scn.AddThreats(3);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetThreats());

		assertInHand(attack);
		assertFalse(scn.FreepsPlayAvailable(attack));
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShadowPossessionInPlayItIsPlayedForNothingAndTheThreatIsStillAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.MoveCardsToHand(attack);

		var stone = scn.GetShadowCard("stone");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(stone, armory);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, stone, armory);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(attack));

		scn.FreepsPlayCard(attack);

		// Only a Shadow artifact and a Shadow condition are in play.
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DISCARD, attack);
		assertInZone(Zone.SUPPORT, stone, armory);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
