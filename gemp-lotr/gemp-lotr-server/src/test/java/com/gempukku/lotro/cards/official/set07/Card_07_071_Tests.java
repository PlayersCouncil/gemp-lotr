package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "7_71");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, vitality 4
					// Unexpected Visitor: [gondor] condition on a [gondor] companion.  'Each minion gains this
					// ability: "Assignment: Assign this minion to bearer of Unexpected Visitor."'
					put("visitor", "7_126");
					// We Hates Them: [gollum] condition on Sméagol.  'Each minion gains this ability: "Assignment:
					// Assign this minion to Sméagol."'  Its regroup ability is never used here.
					put("hates", "7_77");

					// The Balrog, The Terror of Khazad-dûm: [moria] Balrog, strength 17, vitality 5, damage +1; its
					// other text acts only at an underground site
					put("balrog", "12_79");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; Damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its text is about being played
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					// Morgul Spawn: [wraith] Orc, vitality 2.  "While you can spot a Nazgûl, the Free Peoples player
					// must exert a companion to assign this minion to a skirmish."
					put("spawn", "7_200");
					// Úlairë Lemenya: the Nazgûl spotted, vitality 2; her text is about [wraith] conditions
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Sméagol, Always Helps
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype:
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Ring-bound</b>. To play, add a burden.
		 * <b>Assignment:</b> Assign a minion to Sméagol and add 2 threats to exhaust that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Sméagol", card.getBlueprint().getTitle());
		assertEquals("Always Helps", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void SmeagolAddsABurdenWhenPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(smeagol));

		scn.FreepsPlayCard(smeagol);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileNoBurdenCanBeAddedToAHinderedRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(smeagol));

		// No burden can be added to a hindered Ring-bearer.
		scn.HinderCard(frodo);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(smeagol));
		assertInZone(Zone.HAND, smeagol);
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void SmeagolAbilityAssignsMinionAndAdds2ThreatsToExhaustMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		var balrog = scn.GetShadowCard("balrog");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, savage, troop);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsPass();

		// The Shadow player assigns the Troop to the bearer of Unexpected Visitor.
		scn.ShadowUseCardAction(troop);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop));

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertFalse(scn.IsCharAssigned(smeagol));
		assertFalse(scn.IsCharAssigned(balrog));
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);

		// A [moria] Balrog and an [isengard] Uruk-hai; not the Troop, which already has Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balrog, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(balrog);

		// Three companions: room for both threats.
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsCharAssignedAgainst(smeagol, balrog));
		// Vitality 5: exhausted is four exertions.
		assertEquals(4, scn.GetWoundsOn(balrog));
		assertEquals(1, scn.GetVitality(balrog));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertFalse(scn.IsCharAssigned(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AMinionThatCannotExertIsAssignedForTheThreatsAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsUseCardAction(smeagol);

		// The assignment is what he pays: the Runner, vitality 1, may be chosen though it cannot be exerted.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		scn.FreepsChooseCard(runner);

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsCharAssignedAgainst(smeagol, runner));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertFalse(scn.IsCharAssigned(savage));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void SmeagolAbilityCantBeUsedIfNoThreatRoom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var balrog = scn.GetShadowCard("balrog");

		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		// Two companions, Frodo and Sméagol: with 1 threat there is room for one more, and he owes two.
		scn.AddThreats(1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.FreepsActionAvailable(smeagol));

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(smeagol));
	}

	@Test
	public void NotOfferedOnceSmeagolIsAlreadyAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var hates = scn.GetFreepsCard("hates");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, hates);

		var balrog = scn.GetShadowCard("balrog");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsPass();

		// The Shadow player assigns the Savage to Sméagol with the ability We Hates Them gives it.
		scn.ShadowUseCardAction(savage);
		assertTrue(scn.IsCharAssignedAgainst(smeagol, savage));

		// The Balrog is unassigned and there is room for 2 threats, but Sméagol can no longer be assigned.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(balrog));
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());
		assertFalse(scn.FreepsActionAvailable(smeagol));
	}

	@Test
	public void AMorgulSpawnIsPaidForWithAnExertionWhenSmeagolHasItAssignedToHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		var spawn = scn.GetShadowCard("spawn");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsUseCardAction(smeagol);

		assertTrue(scn.FreepsHasCardChoiceAvailable(spawn, lemenya));
		scn.FreepsChooseCard(spawn);

		// With a Nazgûl to spot the Spawn has its price, asked now that the pair is chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, smeagol, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.IsCharAssigned(smeagol));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsCharAssignedAgainst(smeagol, spawn));
		// Vitality 2: exhausted is one exertion.
		assertEquals(1, scn.GetWoundsOn(spawn));
		assertFalse(scn.IsCharAssigned(lemenya));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNobodyAbleToExertAMorgulSpawnIsNotSmeagolsToAssign() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var spawn = scn.GetShadowCard("spawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya, savage);
		// Vitality 4 with 3 wounds, each: neither companion can exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(smeagol, 3);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);

		// The minions that ask no price are his to choose; the Spawn is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(spawn));

		scn.FreepsChooseCard(savage);
		assertTrue(scn.IsCharAssignedAgainst(smeagol, savage));
		assertFalse(scn.IsCharAssigned(spawn));
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(spawn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
