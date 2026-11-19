package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_152_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander", "7_152");
					put("commander2", "7_152");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man; a regroup ability used only on purpose
					// Easterling Axeman: a [raider] Easterling Man; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Desert Soldier: a [raider] Southron Man, strength 9; an optional trigger at the start of its own
					// skirmish (remove (3) to wound), declined
					put("soldier", "4_220");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text

					// Companions, so that 6 threats can lie (Frodo makes the sixth): abilities used only on purpose.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4; a response if Frodo dies
					put("arwen", "3_7");        // Arwen, Elven Rider: vitality 3; a response that discards 3 cards from hand
					// Thrarin, Dwarven Smith: an ally who exerts to take part in skirmishes until the regroup phase
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MumakCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mûmak Commander
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 16
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br>To play, spot a [raider] Man.<br>While you can spot 6 threats, each time this minion wins a skirmish, the companion he was skirmishing is killed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("commander");

		assertEquals("Mûmak Commander", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(16, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedWithASouthronToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(commander);
		assertInZone(Zone.HAND, commander);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		// Enough for him: 8 + 2 roaming at site 2.
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(commander));
		scn.ShadowPlayCard(commander);
		assertInZone(Zone.SHADOW_CHARACTERS, commander);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilARaiderManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var commander = scn.GetShadowCard("commander");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(commander, axeman);
		assertInZone(Zone.HAND, commander, axeman);

		scn.StartGame();
		// A [raider] minion who is no Man, a Man who is not [raider], and the Free Peoples player's own Man.
		scn.MoveMinionsToTable(khamul, brigand);
		scn.SetTwilight(14);
		scn.FreepsPass();

		// 14 set + 2 companions + 2 for the site: enough for the Axeman (2 + 2 roaming) and then for him (8 + 2).
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(commander));

		// An Easterling is a [raider] Man.
		scn.ShadowPlayCard(axeman);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.ShadowPlayAvailable(commander));

		scn.ShadowPlayCard(commander);
		assertInZone(Zone.SHADOW_CHARACTERS, commander);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSixThreatsTheCompanionHeBeatsIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gandalf", "gimli", "sam");

		var commander = scn.GetShadowCard("commander");
		var commander2 = scn.GetShadowCard("commander2");

		scn.StartGame();
		scn.AddThreats(6);
		// Aragorn at 10: beaten by 16, but not overwhelmed, so the loss alone would be a wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		scn.MoveMinionsToTable(commander, commander2);
		assertEquals(6, scn.GetThreats());
		assertEquals(10, scn.GetStrength(aragorn));

		// Boromir waits for his skirmish against the second copy while the first is resolved.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, commander}, new PhysicalCardImpl[]{boromir, commander2});
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// Only the companion he was skirmishing, chosen unasked.
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		// His death takes the 6 threats off and asks for them as wounds.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsDecisionAvailable("Choose characters to wound"));
	}

	@Test
	public void WithFiveThreatsTheCompanionHeBeatsIsOnlyWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gandalf", "gimli", "sam");

		var commander = scn.GetShadowCard("commander");

		scn.StartGame();
		// Five threats, one short, and a burden beside them.
		scn.AddThreats(5);
		scn.AddBurdens(1);
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		scn.MoveMinionsToTable(commander);
		assertEquals(5, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(5, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithSixThreatsNoCompanionIsKilledWhenHeLosesOrWhenAnotherMinionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gandalf", "gimli", "sam");

		var commander = scn.GetShadowCard("commander");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.AddThreats(6);
		// Aragorn at 18 beats the Commander's 16 without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));
		scn.MoveMinionsToTable(commander, soldier);
		assertEquals(6, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, commander}, new PhysicalCardImpl[]{gimli, soldier});

		// Another [raider] Man, the Soldier (9), beats Gimli (6).
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetThreats());

		// The Commander loses to Aragorn.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(commander));
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(commander));
		assertEquals(6, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithSixThreatsAnAllyHeBeatsIsOnlyWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gandalf", "gimli", "sam");
		scn.MoveAlliesToTable(thrarin);

		var commander = scn.GetShadowCard("commander");

		scn.StartGame();
		scn.AddThreats(6);
		// Thrarin at 9 and with vitality 4, so that he survives his own exertion and the loss (no printed card
		// raises them).
		scn.ApplyAdHocModifier(new StrengthModifier(null, thrarin, null, 5));
		scn.ApplyAdHocModifier(new VitalityModifier(null, thrarin, null, 2));
		scn.MoveMinionsToTable(commander);
		assertEquals(6, scn.GetThreats());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, commander);
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		// His win sets his trigger off beside the wound for losing, and it finds no companion to kill.
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveRuleFirst();

		// The ally is no companion: the loss is a wound, and nothing more.
		assertInZone(Zone.SUPPORT, thrarin);
		assertEquals(2, scn.GetWoundsOn(thrarin));
		assertEquals(6, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeHeWinsWithSixThreatsTheCompanionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var arwen = scn.GetFreepsCard("arwen");
		// Seven companions, so that six threats can lie again after one of them is dead.
		scn.MoveCompanionsToTable(aragorn, boromir, gandalf, gimli, sam, arwen);

		var commander = scn.GetShadowCard("commander");

		scn.StartGame();
		scn.AddThreats(6);
		// Both at 10: beaten by 16 and not overwhelmed.  The Commander made fierce, for a second skirmish.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 3));
		scn.MoveMinionsToTable(commander);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, commander, null, Keyword.FIERCE));
		assertEquals(6, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(0, scn.GetThreats());

		// The 6 threat wounds, spread so that nobody dies.
		scn.FreepsChooseCard(gandalf);
		scn.FreepsChooseCard(gandalf);
		scn.FreepsChooseCard(gandalf);
		scn.FreepsChooseCard(sam);
		scn.FreepsChooseCard(sam);
		scn.FreepsChooseCard(sam);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(3, scn.GetWoundsOn(sam));

		// Six threats again, on six companions, for the fierce skirmish.
		assertTrue(scn.InFierceRound());
		scn.AddThreats(6);
		assertEquals(6, scn.GetThreats());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, commander);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DEAD, aragorn, boromir);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, frodo, gimli, arwen);
		assertTrue(scn.FreepsDecisionAvailable("Choose characters to wound"));
	}
}
