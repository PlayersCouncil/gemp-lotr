package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "8_10");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Legolas; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");    // a second Goblin Patrol Troop: wounded by hand, and assigned to nobody
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; his text answers a weapon played on him

					put("wall", "5_33");        // City Wall: [gondor] fortification, bearer strength -3; its ability works only from the support area
					put("numenor", "5_37");     // Men of Numenor: Skirmish event, cost 0; wound a minion bearing a [gondor] fortification
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Legolas, Elven Stalwart
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Archer</b>.<br>While you can spot 3 wounded minions, each minion skirmishing Legolas is strength -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Elven Stalwart", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void WithAThirdMinionWoundedEachMinionSkirmishingLegolasIsStrengthMinus3AndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToHand(numenor);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		// Legolas is an archer: with the Savage the only minion in the archery phase, his archery wound goes
		// on it unasked. The other minions come down after.
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.MoveMinionsToTable(troop, troop2, chieftain);
		// City Wall on the Troop is what lets Men of Numenor wound him in the skirmish. It makes him strength -3
		// throughout.
		scn.AttachCardsTo(troop, wall);
		// A second wound on the Savage and one on the other Troop: three wounds on two minions, so a count of
		// wounds would reach 3 too early.
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(troop2, 1);
		// A wounded companion, who is no minion and is not counted.
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(10, scn.GetStrength(troop));   // 13 - 3 for the wall

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{legolas, savage},
				new PhysicalCardImpl[]{aragorn, troop});
		scn.ShadowAssignToMinions(legolas, chieftain);

		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.IsCharSkirmishingAgainst(legolas, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(legolas, chieftain));
		// Two wounded minions, the Savage (2 wounds) and the second Troop, and a wounded companion: one short.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(13, scn.GetStrength(troop2));

		// A third minion wounded in the skirmish: -3 at once to each minion skirmishing Legolas, the Chieftain
		// who has no wound as well as the Savage who has two.
		scn.FreepsPlayCard(numenor);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(2, scn.GetStrength(savage));       // 5 - 3
		assertEquals(4, scn.GetStrength(chieftain));    // 7 - 3
		assertEquals(6, scn.GetStrength(legolas));
		// The wounded Troop, assigned to Aragorn whose skirmish has not begun, and the wounded Troop assigned to
		// nobody: neither skirmishes Legolas.
		assertEquals(10, scn.GetStrength(troop));       // 13 - 3 for the wall
		assertEquals(13, scn.GetStrength(troop2));

		// Legolas at 6 against 2 + 4 is a tie, which the Shadow side wins, not double: he takes a wound and the
		// Savage's damage +1, and lives.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertInZone(Zone.FREE_CHARACTERS, legolas);

		// Three wounded minions still, and one skirmishing a companion who is not Legolas.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertEquals(10, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
