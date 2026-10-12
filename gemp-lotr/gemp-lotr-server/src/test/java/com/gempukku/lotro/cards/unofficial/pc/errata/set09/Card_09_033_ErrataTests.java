package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_033_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("isildur", "59_33");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, no knight; a maneuver ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, strength 6; Knight is its whole text
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit, strength 3; Knight; a skirmish ability used only on purpose
					put("dwarf", "1_7");        // Dwarf Guard: [dwarven] Dwarf, strength 4, vitality 2, no text in play
					put("narsil", "17_31");     // Narsil, Forged by Telchar: [gondor] artifact for Elendil or Isildur, cost 3; strength +3, damage +1
					put("faithful", "9_34");    // Narsil, Blade of the Faithful: [gondor] artifact for a [gondor] Man; Isildur is strength +1 for each [gondor] artifact
					put("armor", "1_92");       // Armor: [gondor] possession for a Man; changes no strength
					put("barahir", "6_55");     // Ring of Barahir: [gondor] artifact for Aragorn; an optional start-of-turn trigger
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4, no knight
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, strength 6; Knight is its whole text
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit, strength 3; Knight; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: [rohan] Man, strength 6, vitality 3; Valiant is its whole text
					put("dwarf", "1_7");        // Dwarf Guard: [dwarven] Dwarf, strength 4, vitality 2, no text in play
					put("dwarf2", "1_7");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability used only on purpose
					put("narsil", "17_31");     // Narsil, Forged by Telchar: [gondor] artifact for Elendil or Isildur, cost 3; strength +3, damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 and nothing else
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				"59_33",                        // Isildur is the Ring-bearer
				// The Great Ring gives no strength and, while it is not worn, answers no wound; its abilities
				// are used only on purpose.
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void IsildurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Isildur, Bearer of Heirlooms
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Can be the Ring-bearer: True
		 * Game Text: While Isildur is the Ring-bearer, at the start of each skirmish involving him, add 2 burdens or exert 3 companions.
		 * 	While Isildur bears The One Ring or an artifact, each [gondor] knight is strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("isildur");

		assertEquals("Isildur", card.getBlueprint().getTitle());
		assertEquals("Bearer of Heirlooms", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
		assertTrue(card.getBlueprint().canStartWithRing());
		assertFalse(scn.HasKeyword(card, Keyword.KNIGHT));
	}

	@Test
	public void AsTheRingBearerIsildurAddsTwoBurdensAtTheStartOfHisOwnSkirmishAndOfNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var isildur = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarf = scn.GetFreepsCard("dwarf");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(aragorn, dwarf);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage2}, new PhysicalCardImpl[]{isildur, savage});

		// A skirmish that does not involve Isildur, though he is assigned to another: nothing is asked.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(dwarf));

		// Aragorn (8) beats his Uruk Savage (5).
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetBurdens());

		// Isildur, Aragorn and the Dwarf Guard could all exert, so both halves are on offer.
		scn.FreepsResolveSkirmish(isildur);
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Exert"));

		scn.FreepsChoose("burdens");
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(dwarf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AsTheRingBearerIsildurMayExertThreeCompanionsInsteadOfAddingTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var isildur = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var dwarf = scn.GetFreepsCard("dwarf");
		var dwarf2 = scn.GetFreepsCard("dwarf2");
		var grimir = scn.GetFreepsCard("grimir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, guard, dwarf, dwarf2);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 2 with 1 wound: a companion who cannot exert.
		scn.AddWoundsToChar(dwarf2, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, savage);
		scn.FreepsResolveSkirmish(isildur);

		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Exert"));

		scn.FreepsChoose("Exert");
		// Exactly three, from the companions able to exert: Isildur is one of them, the ally is not, and
		// neither is the exhausted Dwarf Guard.
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(isildur, aragorn, guard, dwarf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwarf2, grimir));

		scn.FreepsChooseCards(isildur, aragorn, dwarf);
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(dwarf));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(dwarf2));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOnlyTwoCompanionsAbleToExertTheBurdensAreAddedAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var isildur = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarf = scn.GetFreepsCard("dwarf");
		var grimir = scn.GetFreepsCard("grimir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, dwarf);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Three companions, of whom the Dwarf Guard (vitality 2, 1 wound) cannot exert; and three characters
		// who can, of whom Grimir is no companion.
		scn.AddWoundsToChar(dwarf, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, savage);
		assertEquals(0, scn.GetBurdens());

		scn.FreepsResolveSkirmish(isildur);

		// Exerting 3 companions could not be done in full, so it is not a choice the player may make: the
		// burdens are what happens.
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(dwarf));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AFierceMinionBringsTheChoiceAgainAtIsildursSecondSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var isildur = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var dwarf = scn.GetFreepsCard("dwarf");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, guard, dwarf);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, troop);
		scn.FreepsResolveSkirmish(isildur);

		assertFalse(scn.InFierceRound());
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		scn.FreepsChoose("Exert");
		scn.FreepsChooseCards(aragorn, guard, dwarf);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(dwarf));
		assertEquals(0, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetBurdens());

		// 7 against 13: Isildur loses and takes the skirmish's wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetBurdens());

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(isildur, troop);
		scn.FreepsResolveSkirmish(isildur);

		// Isildur (vitality 3), Aragorn (4) and the Household Guard (3) can each still exert, so the choice
		// is put again.
		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		scn.FreepsChoose("burdens");
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(dwarf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhileIsildurIsNotTheRingBearerNothingIsAskedAtHisSkirmishOrAtTheRingBearers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarf = scn.GetFreepsCard("dwarf");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		// With Aragorn and the Dwarf Guard, three companions besides Frodo could exert.
		scn.MoveCompanionsToTable(isildur, aragorn, dwarf);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		// 3 + 1 for The Ruling Ring + 3 = 7 against the Uruk Savage's 5: Frodo wins his skirmish, so The Ruling
		// Ring's response to a wound is never in the way.
		scn.ApplyAdHocModifier(new StrengthModifier(null, frodo, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, savage}, new PhysicalCardImpl[]{isildur, troop});

		// The Ring-bearer's skirmish begins while Isildur is not in it and is not the Ring-bearer.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(dwarf));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetBurdens());

		// Isildur's own skirmish, with Frodo the Ring-bearer.
		scn.FreepsResolveSkirmish(isildur);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(dwarf));

		// 7 against 13: he loses and takes the one wound of the skirmish.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(dwarf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlayingAnArtifactOnIsildurMakesEachGondorKnightStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var narsil = scn.GetFreepsCard("narsil");
		var armor = scn.GetFreepsCard("armor");
		var barahir = scn.GetFreepsCard("barahir");
		// Every printed knight is a [gondor] card, so no knight of another culture stands here (Q-0213).
		scn.MoveCompanionsToTable(isildur, aragorn, knight, pippin);
		// A [gondor] card that Isildur bears and that is no artifact.
		scn.AttachCardsTo(isildur, armor);
		scn.MoveCardsToHand(narsil);

		scn.StartGame();
		// An artifact that another companion bears (put on after the start of the turn, where its own
		// trigger would have been offered).
		scn.AttachCardsTo(aragorn, barahir);

		// Frodo bears The One Ring, Aragorn an artifact, Isildur only his Armor: nobody is stronger.
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(isildur));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(narsil));

		scn.FreepsPlayCard(narsil);
		assertAttachedTo(narsil, isildur);
		assertEquals(3, scn.GetTwilight());

		// The Man and the Hobbit who are [gondor] knights are each 1 stronger.
		assertEquals(7, scn.GetStrength(knight));
		assertEquals(4, scn.GetStrength(pippin));
		// A [gondor] Man who is no knight, and Isildur himself (7 + 3 for Narsil), are not.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(10, scn.GetStrength(isildur));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheKnightsLoseTheBonusWhenTheArtifactLeavesIsildur() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var isildur = scn.GetFreepsCard("isildur");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var faithful = scn.GetFreepsCard("faithful");
		scn.MoveCompanionsToTable(isildur, knight, pippin);
		// The other artifact Isildur can bear.
		scn.AttachCardsTo(isildur, faithful);

		scn.StartGame();

		assertAttachedTo(faithful, isildur);
		assertEquals(7, scn.GetStrength(knight));
		assertEquals(4, scn.GetStrength(pippin));
		// 7 + 1 by this Narsil's own text, for the one [gondor] artifact in play.
		assertEquals(8, scn.GetStrength(isildur));

		scn.MoveCardsToDiscard(faithful);

		assertEquals(6, scn.GetStrength(knight));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(7, scn.GetStrength(isildur));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileIsildurBearsTheOneRingEachGondorKnightIsStrengthPlus1AndAnArtifactAddsNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var isildur = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var narsil = scn.GetFreepsCard("narsil");
		// Every printed knight is a [gondor] card, so no knight of another culture stands here (Q-0213).
		scn.MoveCompanionsToTable(aragorn, knight, pippin);
		scn.MoveCardsToHand(narsil);

		scn.StartGame();

		// Isildur bears The One Ring and nothing else.  Printed strengths: Gondorian Knight 6, Pippin 3,
		// Aragorn (a [gondor] Man who is no knight) 8, Isildur 7.
		assertEquals(7, scn.GetStrength(knight));
		assertEquals(4, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(isildur));

		scn.FreepsPlayCard(narsil);
		assertAttachedTo(narsil, isildur);

		// The Ring and an artifact together are still one +1.
		assertEquals(7, scn.GetStrength(knight));
		assertEquals(4, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(10, scn.GetStrength(isildur));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
