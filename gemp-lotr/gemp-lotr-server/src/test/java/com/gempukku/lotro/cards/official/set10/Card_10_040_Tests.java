package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("berserker", "10_40");
					put("berserker2", "10_40");
					// Easterling Attacker: a [raider] Easterling Man, vitality 2; "Skirmish: Exert this minion to make
					// him damage +1", not used
					put("attacker", "7_141");
					// Southron Explorer: a [raider] Southron Man, vitality 3, no Easterling; a regroup ability only
					put("explorer", "4_250");
					// Corsair Buccaneer: a [raider] corsair Man, vitality 2; a "when you play" trigger only
					put("buccaneer", "8_53");
					// Ulaire Attea, Khamul of the East: a [raider] Easterling who is a Nazgul and no Man; his text
					// needs 3 burdens
					put("khamul", "103_61");
					// Dunlending Brigand: a [dunland] Man, a Man who is not [raider]; no text
					put("brigand", "4_10");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability not used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingBerserkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Easterling Berserker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>. To play, spot an Easterling.<br>While this minion is unwounded, he is <b>damage +2</b>.<br><b>Skirmish:</b> Wound 2 other [raider] Men to heal this minion.
		*/

		var scn = GetScenario();

		var berserker = scn.GetFreepsCard("berserker");

		assertEquals("Easterling Berserker", berserker.getBlueprint().getTitle());
		assertNull(berserker.getBlueprint().getSubtitle());
		assertFalse(berserker.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, berserker.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, berserker.getBlueprint().getCulture());
		assertEquals(CardType.MINION, berserker.getBlueprint().getCardType());
		assertEquals(Race.MAN, berserker.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(berserker, Keyword.EASTERLING));
		assertEquals(6, berserker.getBlueprint().getTwilightCost());
		assertEquals(11, berserker.getBlueprint().getStrength());
		assertEquals(3, berserker.getBlueprint().getVitality());
		assertEquals(4, berserker.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnEasterlingIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var attacker = scn.GetShadowCard("attacker");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(berserker, attacker);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A [raider] Man who is no Easterling is not enough.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(berserker));

		scn.ShadowPlayCard(attacker);
		assertTrue(scn.ShadowPlayAvailable(berserker));

		scn.ShadowPlayCard(berserker);
		assertInZone(Zone.SHADOW_CHARACTERS, berserker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingAnEasterlingWhoIsNoMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(berserker);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(berserker));
		scn.ShadowPlayCard(berserker);
		assertInZone(Zone.SHADOW_CHARACTERS, berserker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileUnwoundedHeIsDamagePlusTwoAndAWoundedCopyAndAnotherMinionAreNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var berserker2 = scn.GetShadowCard("berserker2");
		var attacker = scn.GetShadowCard("attacker");

		scn.StartGame();
		scn.MoveMinionsToTable(berserker, berserker2, attacker);
		scn.AddWoundsToChar(berserker2, 1);

		assertEquals(0, scn.GetWoundsOn(berserker));
		assertEquals(1, scn.GetWoundsOn(berserker2));
		assertEquals(0, scn.GetWoundsOn(attacker));

		assertEquals(2, scn.GetKeywordCount(berserker, Keyword.DAMAGE));
		// A wounded copy has no bonus from its own text nor from the unwounded one's.
		assertEquals(0, scn.GetKeywordCount(berserker2, Keyword.DAMAGE));
		// Another unwounded minion beside them gets nothing.
		assertEquals(0, scn.GetKeywordCount(attacker, Keyword.DAMAGE));
	}

	@Test
	public void SkirmishAbilityWoundsTwoOtherRaiderMenToHealHimOneWoundAtATime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var attacker = scn.GetShadowCard("attacker");
		var explorer = scn.GetShadowCard("explorer");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker, attacker, explorer, buccaneer, khamul, brigand);
		scn.AddWoundsToChar(berserker, 2);
		// Vitality 2 with 1 wound: exhausted, but a wound may still be placed on him, and kills him.
		scn.AddWoundsToChar(attacker, 1);
		assertEquals(2, scn.GetWoundsOn(berserker));
		assertEquals(1, scn.GetWoundsOn(attacker));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, berserker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(berserker));
		assertEquals(1, scn.GetWoundsOn(attacker));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(buccaneer));
		assertEquals(0, scn.GetKeywordCount(berserker, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(berserker));

		scn.ShadowUseCardAction(berserker);
		// [raider] Men of three kinds, the exhausted one among them; not the Berserker himself, not the [raider]
		// Nazgul, not the [dunland] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(attacker, explorer, buccaneer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(berserker, khamul, brigand, aragorn));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(attacker, explorer);

		assertInDiscard(attacker);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(buccaneer));
		// One wound healed of two: still wounded, so still no damage bonus.
		assertEquals(1, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetKeywordCount(berserker, Keyword.DAMAGE));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(berserker));
		scn.ShadowUseCardAction(berserker);
		// Only the Explorer and the Buccaneer are left to pay with.
		assertEquals(2, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(buccaneer));
		assertEquals(0, scn.GetWoundsOn(berserker));
		// Unwounded again: damage +2.
		assertEquals(2, scn.GetKeywordCount(berserker, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityHealsHimWhileAnotherMinionIsTheOneSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var attacker = scn.GetShadowCard("attacker");
		var explorer = scn.GetShadowCard("explorer");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker, attacker, explorer, buccaneer);
		scn.AddWoundsToChar(berserker, 1);
		assertEquals(1, scn.GetWoundsOn(berserker));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, attacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Aragorn fights the Attacker; the Berserker is not in this skirmish, and the text does not ask him to be.
		assertTrue(scn.ShadowActionAvailable(berserker));
		scn.ShadowUseCardAction(berserker);
		scn.ShadowChooseCards(explorer, buccaneer);

		assertEquals(0, scn.GetWoundsOn(berserker));
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(buccaneer));
		assertEquals(0, scn.GetWoundsOn(attacker));
		assertEquals(2, scn.GetKeywordCount(berserker, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOnlyOneOtherRaiderManTheSkirmishAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var explorer = scn.GetShadowCard("explorer");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker, explorer, khamul, brigand);
		scn.AddWoundsToChar(berserker, 1);
		assertEquals(1, scn.GetWoundsOn(berserker));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, berserker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// One [raider] Man beside him, and two minions who are not [raider] Men: the cost cannot be paid in full.
		assertEquals(1, scn.GetWoundsOn(berserker));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(berserker));
	}
}
