package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_09_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("linnar", "9_5");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1; a skirmish ability, never used
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2;
												// "Maneuver: Exert Thrarin to allow him to participate in archery fire and skirmishes until the regroup phase."
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, strength 8, vitality 4; a maneuver ability only

					// Cards to hold in hand, none of them playable in a skirmish.
					put("delving", "1_6");      // Delving: [dwarven] Fellowship event
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] support-area condition
					put("bow", "1_41");         // Elven Bow: [elven] possession; no Elf to bear it

					// Fierce minions with nothing else that reaches these boards.
					put("pursuer", "4_190");    // Uruk Pursuer: strength 5, vitality 3, fierce; its texts are about unbound Hobbits
					put("pursuer2", "4_190");
					put("seeker", "4_195");     // Uruk Seeker: strength 6, vitality 2, fierce; stronger only against a bearer of a search card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LinnarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Linnar, Dwarven Lord
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Each time a Dwarf wins a fierce skirmish, you may heal that Dwarf.<br><b>Skirmish:</b> Discard a [dwarven] card from hand to make a Dwarf <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("linnar");

		assertEquals("Linnar", card.getBlueprint().getTitle());
		assertEquals("Dwarven Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AWoundedDwarfWhoWinsAFierceSkirmishMayBeHealedButNotWhenHeWinsTheFirstRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var linnar = scn.GetFreepsCard("linnar");
		var gimli = scn.GetFreepsCard("gimli");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(linnar, gimli);
		scn.AddWoundsToChar(linnar, 1);
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(linnar, pursuer);
		scn.FreepsResolveSkirmish(linnar);
		scn.PassSkirmishActions();

		// Linnar 7 beats the fierce Pursuer 5 in the first round: no fierce skirmish, nothing offered.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.InFierceRound());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(linnar));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetWoundsOn(linnar));
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(linnar, pursuer);
		scn.FreepsResolveSkirmish(linnar);
		scn.PassFierceSkirmishActions();

		// He beats it again in its fierce skirmish: he may heal, and the heal is his, not the other wounded Dwarf's.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(linnar));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(linnar));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachDwarfWhoWinsAFierceSkirmishIsOfferedHisOwnHealAndAManWhoWinsIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var linnar = scn.GetFreepsCard("linnar");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(linnar, gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, pursuer2, seeker);
		scn.SkipToPhase(Phase.MANEUVER);

		// Thrarin takes part in skirmishes, and keeps the wound of his exertion. A strength of 7, by hand, lets him
		// beat a Pursuer.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.ApplyAdHocModifier(new StrengthModifier(null, thrarin, null, 3));

		// Nobody is assigned in the first round.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, pursuer}, new PhysicalCardImpl[]{thrarin, pursuer2},
				new PhysicalCardImpl[]{aragorn, seeker});

		// Gimli 6 beats a Pursuer 5, and his heal is offered and taken.
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(linnar));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Thrarin, an ally, beats the other Pursuer: offered a second time, and declined.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassFierceSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(pursuer2));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(linnar));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(thrarin));

		// Aragorn 8 beats the Seeker 6: he is no Dwarf, and nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(linnar));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(thrarin));
	}

	@Test
	public void NotOfferedWhenTheDwarfWhoWinsTheFierceSkirmishHasNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var linnar = scn.GetFreepsCard("linnar");
		var gimli = scn.GetFreepsCard("gimli");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(linnar, gimli);
		// Another Dwarf has a wound to heal, but he is not the one who wins.
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		assertEquals(0, scn.GetWoundsOn(linnar));
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(linnar, pursuer);
		scn.FreepsResolveSkirmish(linnar);
		scn.PassFierceSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(linnar));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(linnar));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(gimli));
	}

	@Test
	public void DiscardingADwarvenCardFromHandMakesADwarfDamagePlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var linnar = scn.GetFreepsCard("linnar");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var delving = scn.GetFreepsCard("delving");
		var stairs = scn.GetFreepsCard("stairs");
		var bow = scn.GetFreepsCard("bow");
		var pursuer = scn.GetShadowCard("pursuer");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(linnar, gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(delving, stairs, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{linnar, pursuer}, new PhysicalCardImpl[]{gimli, seeker});
		scn.FreepsResolveSkirmish(linnar);

		assertEquals(0, scn.GetKeywordCount(linnar, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(linnar));

		scn.FreepsUseCardAction(linnar);
		// A [dwarven] event or a [dwarven] condition; not an [elven] card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(delving, stairs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bow));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(stairs);
		assertInZone(Zone.DISCARD, stairs);
		assertInZone(Zone.HAND, delving);
		assertInZone(Zone.HAND, bow);

		// Any Dwarf: Linnar in the skirmish, Gimli in a skirmish still to come, Grimir an ally; not a Man, nor the
		// minion Linnar is fighting.
		assertTrue(scn.FreepsHasCardChoiceAvailable(linnar, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, pursuer));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// Gimli's printed damage +1 and this one.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(linnar, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Linnar 7 beats the Pursuer 5. In the next skirmish, a phase of its own, the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishing(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithNoDwarvenCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var linnar = scn.GetFreepsCard("linnar");
		var bow = scn.GetFreepsCard("bow");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(linnar);
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(linnar, pursuer);
		scn.FreepsResolveSkirmish(linnar);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.HAND, bow);
		assertFalse(scn.FreepsActionAvailable(linnar));
	}
}
