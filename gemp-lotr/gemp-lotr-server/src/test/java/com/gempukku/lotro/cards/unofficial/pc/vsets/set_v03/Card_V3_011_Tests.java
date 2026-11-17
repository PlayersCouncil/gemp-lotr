package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_011_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fickle", "103_11");
					put("pippin", "1_306");         // Pippin, Friend to Frodo: [shire] companion, unbound, cost 1; his text is about [shire] tales
					put("gaffer", "1_291");         // The Gaffer: a [shire] Hobbit who is an ally
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit (a Man as well only beside 3 [rohan]
					// companions or Eowyn); his trigger answers another [rohan] Man exerting, and there is none here
					put("holbytla", "103_85");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("glorfindel", "9_16");      // Glorfindel, Revealed in Wrath: strength 9, vitality 3; his skirmish ability is never used
					put("gimli", "1_13");           // Gimli, Son of Gloin: strength 6, vitality 3
					put("smeagol", "6_45");         // Smeagol, Poor Creature: [gollum] companion, strength 3, vitality 4
					put("ruin", "102_52");          // Now for Ruin: each time a minion is killed, spot 2 valiant companions to add a [rohan] token here

					put("gollum", "9_28");          // Gollum, Dark as Darkness: [gollum] minion, strength 5, vitality 4; abilities never used here
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: the Gollum who is [wraith]; strength 5, vitality 4
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: strength 8, vitality 8; fierce, enduring
					put("eater", "8_25");           // Shelob, Eater of Light: strength 8, vitality 8; fierce and not enduring
					put("troop", "1_177");          // Goblin Patrol Troop: [moria], strength 13, vitality 3, no text
					put("savage", "1_151");         // Uruk Savage: [isengard], strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FickleLoyaltiesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Fickle Loyalties
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [shire] companions.
		 * 	Each time Gollum loses a skirmish, make another minion strength -1 until the regroup phase.
		 * 	Each time Shelob loses a skirmish, kill Gollum.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fickle");

		assertEquals("Fickle Loyalties", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayableForOneOnceASecondShireCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fickle = scn.GetFreepsCard("fickle");
		var pippin = scn.GetFreepsCard("pippin");
		var gaffer = scn.GetFreepsCard("gaffer");
		var holbytla = scn.GetFreepsCard("holbytla");
		var frodo = scn.GetRingBearer();
		// Frodo is the one [shire] companion. Beside him are a [shire] Hobbit who is an ally, and a
		// companion who is a Hobbit and not [shire]. The second to count is unbound, as Frodo is not.
		scn.MoveCompanionsToTable(holbytla);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToHand(fickle, pippin);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, holbytla);
		assertInZone(Zone.SUPPORT, gaffer);
		assertInZone(Zone.HAND, fickle);
		assertFalse(scn.FreepsPlayAvailable(fickle));

		scn.FreepsPlayCard(pippin);

		// 1 for Pippin.
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(fickle));

		scn.FreepsPlayCard(fickle);

		assertInZone(Zone.SUPPORT, fickle);
		assertNotAttachedTo(fickle, frodo);
		assertNotAttachedTo(fickle, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GollumLosingASkirmishMakesAnotherMinionOfTheOwnersChoiceWeakerUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fickle = scn.GetFreepsCard("fickle");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var frodo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.MoveCardsToSupportArea(fickle);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, gollum}, new PhysicalCardImpl[]{glorfindel, troop});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));

		// Gollum, strength 5, loses to Aragorn, strength 8.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// Minions of two cultures; Gollum himself is still in play and is not one of them.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gollum, aragorn, glorfindel, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(troop);

		assertEquals(12, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(gollum));

		// Still weaker in the skirmish it fights afterwards: 12 against Glorfindel's 9.
		scn.FreepsResolveSkirmish(glorfindel);
		assertTrue(scn.IsCharSkirmishing(troop));
		assertEquals(12, scn.GetStrength(troop));

		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(glorfindel));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachSkirmishGollumLosesWeakensAMinionAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fickle = scn.GetFreepsCard("fickle");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gollum = scn.GetShadowCard("wraithgollum");
		var shelob = scn.GetShadowCard("shelob");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(fickle);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob, savage);
		// Fierce, so that Gollum fights twice in one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, gollum, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(shelob));
		assertEquals(5, scn.GetStrength(savage));

		// Gollum, strength 5, loses to Aragorn, strength 8.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// The other minion of the [gollum] culture is as good a choice as any.
		assertTrue(scn.FreepsHasCardChoiceAvailable(shelob, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gollum));
		scn.FreepsChooseCard(shelob);

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(7, scn.GetStrength(shelob));
		assertEquals(5, scn.GetStrength(savage));

		// The fierce skirmishes: Gollum loses to Aragorn again. Shelob's own fierce skirmish is still to
		// come, so the turn has not reached the regroup phase when her strength is read.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, gollum}, new PhysicalCardImpl[]{gimli, shelob});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertTrue(scn.FreepsHasCardChoiceAvailable(shelob, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gollum));
		scn.FreepsChooseCard(shelob);

		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(6, scn.GetStrength(shelob));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ShelobLosingASkirmishKillsGollumEachTimeAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fickle = scn.GetFreepsCard("fickle");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var smeagol = scn.GetFreepsCard("smeagol");
		var shelob = scn.GetShadowCard("eater");
		var gollum = scn.GetShadowCard("gollum");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var savage = scn.GetShadowCard("savage");
		var ruin = scn.GetFreepsCard("ruin");
		scn.MoveCompanionsToTable(glorfindel, smeagol);
		scn.MoveCardsToSupportArea(fickle, ruin);
		// Now for Ruin gains a token for a minion that is killed and none for one that is discarded;
		// it needs 2 valiant companions to spot.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.COMPANION, null, Keyword.VALIANT));

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(glorfindel, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(glorfindel);

		assertEquals(9, scn.GetStrength(glorfindel));
		assertEquals(8, scn.GetStrength(shelob));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(0, scn.GetCultureTokensOn(ruin));

		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// Gollum had all 4 of his vitality left, and was killed. Smeagol is not Gollum.
		assertInZone(Zone.DISCARD, gollum);
		assertEquals(1, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.SHADOW_CHARACTERS, shelob, savage);
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(savage));

		// Another Gollum arrives, of another culture, and Shelob loses her fierce skirmish too.
		scn.MoveMinionsToTable(wraithgollum);
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(glorfindel, shelob);

		assertInZone(Zone.SHADOW_CHARACTERS, wraithgollum);

		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DISCARD, wraithgollum);
		assertEquals(2, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob, savage);
		assertEquals(2, scn.GetWoundsOn(shelob));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingHappensWhenGollumWinsSmeagolOrAnotherMinionLosesOrShelobWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fickle = scn.GetFreepsCard("fickle");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(smeagol, aragorn, gimli);
		scn.MoveCardsToSupportArea(fickle);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{smeagol, gollum},
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{gimli, shelob});

		// Smeagol, strength 3, loses to Gollum, strength 5.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// The Savage, strength 5, loses to Aragorn, strength 8.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Shelob, strength 8, beats Gimli, strength 6.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(savage));
		// Shelob is fierce, so the next thing is the fierce assignment phase.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
