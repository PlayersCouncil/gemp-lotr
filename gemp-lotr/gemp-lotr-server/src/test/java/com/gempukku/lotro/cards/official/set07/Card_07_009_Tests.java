package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "7_9");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, printed damage +1; skirmish ability only
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, not unique; its text is about its own play
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; his fellowship ability is used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; maneuver ability only
					put("risk1", "7_4");        // Calculated Risk: Fellowship event, "Spot a Dwarf and add a threat to draw 3 cards"
					put("risk2", "7_4");
					put("risk3", "7_4");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimlisBattleAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gimli's Battle Axe, Trusted Weapon
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a Dwarf.
		 * While you can spot a threat, bearer is <b>damage +1</b>.
		 * While you can spot 2 threats, bearer is strength +1.
		 * While you can spot 3 threats, the fellowship archery total is +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Gimli's Battle Axe", card.getBlueprint().getTitle());
		assertEquals("Trusted Weapon", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBeBorneByADwarfCompanionOrAllyAndByNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(axe);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);
		// A Dwarf companion and a Dwarf ally; not a Man, nor the Hobbit Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(axe, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachThreatAddedSwitchesOnTheNextBonusDamageAtOneStrengthAtTwoArcheryAtThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var risk1 = scn.GetFreepsCard("risk1");
		var risk2 = scn.GetFreepsCard("risk2");
		var risk3 = scn.GetFreepsCard("risk3");
		// Four companions with Frodo, so that three threats can lie.
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToHand(risk1, risk2, risk3);

		scn.StartGame();
		// A burden beside the threats: a token that is no threat counts for nothing.
		scn.AddBurdens(1);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		// Gimli's own printed damage +1; 6 + 2 for the axe's printed strength.
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(risk1);
		assertEquals(1, scn.GetThreats());
		// One threat: the bearer is damage +1, and only the bearer.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(risk2);
		assertEquals(2, scn.GetThreats());
		// Two threats: the bearer is strength +1 as well, and the other Dwarf is not.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(risk3);
		assertEquals(3, scn.GetThreats());
		// Three threats: the fellowship archery total is +1 (no archer in the fellowship), the minions' is not.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByADwarfAllyHeIsDamagePlus1AtAThreatAndStrengthPlus1AtTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Two companions, so that two threats can lie.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(grimir, axe);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		// 3 + 2 for the axe's printed strength.
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(grimir));

		scn.AddThreats(1);
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(grimir));

		scn.AddThreats(1);
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(grimir));
	}

	@Test
	public void BorneByAHinderedGimliItGivesHimNothingButTheFellowshipArcheryTotalIsStillPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.AttachCardsTo(gimli, axe);

		scn.StartGame();
		scn.AddThreats(3);

		assertEquals(3, scn.GetThreats());
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.HinderCard(gimli);
		// The axe's damage +1 does not reach him (his own printed +1 is all that is read); the archery total does not
		// rely on him, and stays +1.  (His strength is not read: the rig reads a hindered card's printed numbers.)
		assertAttachedTo(axe, gimli);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.RestoreCard(gimli);
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(1, scn.GetFreepsArcheryTotal());
	}
}
