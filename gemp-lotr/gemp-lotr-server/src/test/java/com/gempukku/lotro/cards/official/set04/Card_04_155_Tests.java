package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_155_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haunting", "4_155");
					put("grima", "4_154");      // Gríma, Wormtongue: [isengard] Man, vitality 3; a maneuver ability needing a companion bearing 3 cards
					put("grima2", "12_68");     // Gríma, Betrayer of Rohan: [men] Man, vitality 3, lurker; answers a companion losing to a [men] minion
					put("traitor", "6_72");     // Rohirrim Traitor: [isengard] Man who is no Gríma, vitality 2; strength +2 while a companion bears an [isengard] condition
					put("slayer", "1_153");     // Uruk Slayer: [isengard] Uruk-hai, strength 9, damage +1; Skirmish: remove (1) to make himself strength +1
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1 and nothing else

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound companion; Maneuver: exert Aragorn to make him defender +1
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound companion, strength 6; Skirmish: exert Gimli to make him strength +2
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: unbound Elf companion, strength 6; stronger only against a Nazgul
					put("sword", "4_64");       // Elven Sword: possession on an Elf; Skirmish: exert bearer (or discard 2 cards) to make a minion skirmishing bearer strength -1
					put("smeagol", "5_29");     // Smeagol, Slinker: Ring-bound companion; Skirmish: add a burden to make Smeagol strength +2 and take no wounds
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haunting", "4_155");
					put("grima", "4_154");      // Gríma, Wormtongue: [isengard] Man, vitality 3; a maneuver ability only
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc with no text, vitality 3
					put("rumil", "1_57");       // Rúmil, Elven Protector: ally, vitality 3, "Archery: Exert Rúmil to wound an Orc."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HauntingHerStepsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Haunting Her Steps
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a special ability of an ally or unbound companion is used, exert Gríma to cancel that action.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haunting");

		assertEquals("Haunting Her Steps", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseExertsGrimaToCancelASpecialAbilityOfAnAllyAndItsCostStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var haunting = scn.GetShadowCard("haunting");
		var grima = scn.GetShadowCard("grima");
		var troop = scn.GetShadowCard("troop");
		var rumil = scn.GetFreepsCard("rumil");
		scn.MoveAlliesToTable(rumil);
		scn.MoveCardsToHand(haunting);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(rumil));

		// The Troop is the only Orc, so Rúmil's ability would wound it with nothing asked.
		scn.FreepsUseCardAction(rumil);
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertTrue(scn.ShadowPlayAvailable(haunting));

		scn.ShadowPlayCard(haunting);
		assertEquals(1, scn.GetWoundsOn(grima));
		assertInZone(Zone.DISCARD, haunting);
		// The ability is cancelled: no wound.  Its cost stays paid.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ResponseExertsTheOtherCulturesGrimaToCancelASpecialAbilityOfAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haunting = scn.GetShadowCard("haunting");
		var grima = scn.GetShadowCard("grima2");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(haunting);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(haunting);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(aragorn));

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(haunting));

		scn.ShadowPlayCard(haunting);
		// The [men] Gríma pays.
		assertEquals(1, scn.GetWoundsOn(grima));
		assertInDiscard(haunting);
		// Cancelled: Aragorn's exertion stays paid and he is not defender +1.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableForTheAbilitiesOfARingBoundCompanionAPossessionOrAMinionButForAnUnboundCompanionsInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haunting = scn.GetShadowCard("haunting");
		var grima = scn.GetShadowCard("grima");
		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var sword = scn.GetFreepsCard("sword");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(gimli, arwen, smeagol);
		scn.AttachCardsTo(arwen, sword);
		scn.MoveCardsToHand(haunting);
		assertAttachedTo(sword, arwen);
		assertInHand(haunting);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, slayer, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, slayer}, new PhysicalCardImpl[]{gimli, savage});
		// Gríma fights nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);
		scn.SetTwilight(1);

		assertInHand(haunting);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(9, scn.GetStrength(slayer));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetBurdens());

		// A possession's special ability, paid for by its unbound bearer (the Free Peoples hand is empty, so she
		// exerts): the ability is the Sword's, not Arwen's, and it is not answered.
		scn.FreepsUseCardAction(sword);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(8, scn.GetStrength(slayer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(haunting));

		// The Shadow player's own minion's special ability: not answered.
		scn.ShadowUseCardAction(slayer);
		assertEquals(0, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(slayer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// A Ring-bound companion's special ability (Smeagol is in no skirmish): not answered.
		scn.FreepsUseCardAction(smeagol);
		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(haunting));
		assertInHand(haunting);
		assertEquals(0, scn.GetWoundsOn(grima));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));

		// An unbound companion's special ability, with the same Gríma: offered.
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowPlayAvailable(haunting));
		scn.ShadowPass();
		assertInHand(haunting);
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWhileGrimaIsExhaustedThoughAnotherIsengardManCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haunting = scn.GetShadowCard("haunting");
		var grima = scn.GetShadowCard("grima");
		var traitor = scn.GetShadowCard("traitor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(haunting);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, traitor);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(grima, 2);
		assertEquals(2, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(traitor));
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(haunting);
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		scn.FreepsUseCardAction(aragorn);

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(haunting));
		assertInHand(haunting);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(2, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(traitor));
	}
}
