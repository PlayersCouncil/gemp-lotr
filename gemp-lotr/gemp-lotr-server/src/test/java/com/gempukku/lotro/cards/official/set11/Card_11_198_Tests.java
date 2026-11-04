package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_198_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patrol", "11_198");
					put("patrol2", "11_198");
					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2, damage +1 and no other text
					put("cuguard2", "10_81");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4, resistance 6; a
					                            // maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, resistance 5; abilities used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf Long I Fell spots; strength text only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally (resistance 0), vitality 2;
					                            // "Maneuver: Exert Thrarin to allow him to participate in ... skirmishes"
					put("fell", "4_97");        // Long I Fell: Skirmish event, spot Gandalf; the Shadow player may make the Free
					                            // Peoples player wound a minion of his choice, in any skirmish, to prevent its effect
					put("fell2", "4_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PatrolofUrukhaiStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Patrol of Uruk-hai
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 11
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While this minion is skirmishing a Free Peoples character who has resistance 5 or less, this minion cannot take wounds.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("patrol");

		assertEquals("Patrol of Uruk-hai", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileSkirmishingACompanionOfResistance5ItCannotBeWoundedAndOtherwiseItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var patrol2 = scn.GetShadowCard("patrol2");
		var cuguard = scn.GetShadowCard("cuguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		var fell2 = scn.GetFreepsCard("fell2");
		scn.MoveCompanionsToTable(aragorn, sam, gandalf);
		scn.MoveCardsToHand(fell, fell2);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, patrol2, cuguard);

		// No burdens: Aragorn is resistance 6, Sam 5.
		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(sam));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, patrol}, new PhysicalCardImpl[]{sam, patrol2});
		scn.ShadowAssignToMinions(sam, cuguard);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		// Skirmishing resistance 6, this copy may be wounded; the other copy is assigned to Sam (resistance 5) but
		// not yet skirmishing him, and may be wounded too.
		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, patrol2, cuguard));
		scn.FreepsChooseCard(patrol);
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertEquals(0, scn.GetWoundsOn(patrol2));
		assertEquals(0, scn.GetWoundsOn(cuguard));

		// Aragorn (8) loses to the copy (11) and takes two wounds, damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPlayCard(fell2);
		scn.ShadowChooseYes();
		// Skirmishing Sam, resistance 5: this copy cannot take wounds.  The Guard in the same skirmish, and the
		// first copy, now in no skirmish, still can.
		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, cuguard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(patrol2));
		scn.FreepsChooseCard(cuguard);

		assertEquals(1, scn.GetWoundsOn(cuguard));
		assertEquals(0, scn.GetWoundsOn(patrol2));
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAnAllyWhoHasNoResistanceItCannotBeWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var cuguard = scn.GetShadowCard("cuguard");
		var cuguard2 = scn.GetShadowCard("cuguard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thrarin = scn.GetFreepsCard("thrarin");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, cuguard, cuguard2);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thrarin, patrol}, new PhysicalCardImpl[]{aragorn, cuguard});
		scn.ShadowAssignToMinions(aragorn, cuguard2);
		scn.FreepsResolveSkirmish(thrarin);

		// An ally is a Free Peoples character of resistance 0 (CR 4.2 "resistance").
		assertEquals(0, scn.GetResistance(thrarin));
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(cuguard, cuguard2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(patrol));
		scn.FreepsChooseCard(cuguard);

		assertEquals(1, scn.GetWoundsOn(cuguard));
		assertEquals(0, scn.GetWoundsOn(cuguard2));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
