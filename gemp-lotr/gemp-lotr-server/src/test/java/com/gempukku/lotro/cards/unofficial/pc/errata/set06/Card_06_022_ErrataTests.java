package com.gempukku.lotro.cards.unofficial.pc.errata.set06;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_06_022_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "56_22");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: unique Elf companion; strength +2 only at sites 6 to 8
					put("elf1", "1_53");        // Lórien Elf: Elf companion, not unique, cost 1, "To play, spot an Elf."
					put("elf2", "1_53");
					put("elf3", "1_53");
					put("orophin", "1_56");     // Orophin, Lórien Bowman: Elf ally (an archery ability)
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: Man companion, Defender +1 and no other text
					put("elfsong", "1_39");     // Elf-song: an [elven] event
					put("sleep", "1_84");       // Sleep, Caradhras: a [gandalf] event

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NaithTroopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Naith Troop
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant.</b>
		 * 	To play, spot 3 Elf companions. <b>Skirmish:</b> Exert Naith Troop to discard the top card of your draw deck. If it is an [elven] card, make a minion skirmishing Naith Troop strength –2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troop");

		assertEquals("Naith Troop", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void PlaysForThreeOnlyOnceThreeElfCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetFreepsCard("troop");
		var haldir = scn.GetFreepsCard("haldir");
		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(haldir, elf1, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(troop, elf2);

		scn.StartGame();
		// An [elven] Man companion: of the culture, not of the race.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.ELVEN));

		// Two Elf companions (one unique, one not), an Elf ally and an [elven] Man companion: not 3 Elf companions.
		assertFalse(scn.FreepsPlayAvailable(troop));

		scn.FreepsPlayCard(elf2);
		assertTrue(scn.FreepsPlayAvailable(troop));

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(troop);
		assertEquals(twilight + 3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, troop);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionExertsHimToDiscardTheTopCardAndAnElvenCardWeakensAMinionSkirmishingHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetFreepsCard("troop");
		var elf1 = scn.GetFreepsCard("elf1");
		var elfsong = scn.GetFreepsCard("elfsong");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCompanionsToTable(troop, elf1);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.MoveCardsToTopOfDeck(elfsong);
		var shadowTop = scn.GetShadowTopOfDeck();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{troop, runner1}, new PhysicalCardImpl[]{elf1, runner3});
		scn.ShadowAssignToMinions(troop, runner2);
		scn.FreepsResolveSkirmish(troop);

		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(5, scn.GetStrength(runner1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(troop));

		scn.FreepsUseCardAction(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(elfsong);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertSame(shadowTop, scn.GetShadowTopOfDeck());

		// Both minions in his skirmish; not the one assigned to the Lórien Elf, whose skirmish has not begun.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner1, runner2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner3));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner1);

		assertEquals(3, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(5, scn.GetStrength(runner3));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ANonElvenTopCardIsDiscardedAndWeakensNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetFreepsCard("troop");
		var sleep = scn.GetFreepsCard("sleep");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(troop);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.MoveCardsToTopOfDeck(sleep);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(troop, runner1);
		scn.FreepsResolveSkirmish(troop);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(5, scn.GetStrength(runner1));

		scn.FreepsUseCardAction(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(sleep);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetStrength(runner1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnElvenCompanionOnTopWeakensHisMinionUntilTheEndOfHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetFreepsCard("troop");
		var elf3 = scn.GetFreepsCard("elf3");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(troop);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.MoveCardsToTopOfDeck(elf3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(troop, party);
		scn.FreepsResolveSkirmish(troop);

		assertEquals(9, scn.GetStrength(party));
		scn.FreepsUseCardAction(troop);
		assertInDiscard(elf3);
		assertEquals(7, scn.GetStrength(party));
		assertEquals(1, scn.GetWoundsOn(troop));

		// Strength 8 against 7 at resolution: Naith Troop wins. Against 9 he would lose and take 2 more wounds.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(1, scn.GetWoundsOn(troop));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(9, scn.GetStrength(party));
	}

	@Test
	public void InAnotherCompanionsSkirmishAnElvenCardWeakensNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetFreepsCard("troop");
		var elf1 = scn.GetFreepsCard("elf1");
		var elfsong = scn.GetFreepsCard("elfsong");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(troop, elf1);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.MoveCardsToTopOfDeck(elfsong);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{troop, runner2}, new PhysicalCardImpl[]{elf1, runner1});
		scn.FreepsResolveSkirmish(elf1);

		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(troop));

		// Runner 1 is skirmishing the Lórien Elf; runner 2 is assigned to Naith Troop, whose skirmish has not begun.
		scn.FreepsUseCardAction(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(elfsong);
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedInTheManeuverPhaseNorOnceHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetFreepsCard("troop");
		var sleep = scn.GetFreepsCard("sleep");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.MoveCardsToTopOfDeck(sleep);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(troop, runner1);
		scn.FreepsResolveSkirmish(troop);
		assertTrue(scn.FreepsActionAvailable(troop));

		scn.FreepsUseCardAction(troop);
		// Vitality 3 with 2 wounds: exhausted.
		assertEquals(2, scn.GetWoundsOn(troop));
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(troop));
	}
}
