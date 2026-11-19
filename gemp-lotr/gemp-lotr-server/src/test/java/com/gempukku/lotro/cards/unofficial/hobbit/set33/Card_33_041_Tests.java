package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_041_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  The totals are read in the fellowship phase of turn 1, the Troop cheated in.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "33_41");
					// [dwarven] artifacts: one for a support area, one borne.
					put("arkenstone", "31_7");   // The Arkenstone: support area; Fellowship and Regroup abilities, not used
					put("thorin", "30_19");      // Thorin, Oakenshield: [dwarven] Dwarf companion; a Fellowship ability, not used
					put("orcrist", "30_17");     // Orcrist: [dwarven] artifact hand weapon, bearer must be Thorin
					// [dwarven] cards that are no artifact, and an artifact of another culture.
					put("balin", "30_2");        // Balin, Brother of Dwalin: [dwarven] Dwarf companion; a Fellowship ability, not used
					put("axe", "31_1");          // Dwarven Axe: [dwarven] possession hand weapon, on a [dwarven] character
					put("ponies", "33_6");       // Dwarven Ponies: [dwarven] possession, support area; texts not used
					put("sting", "30_47");       // Sting: [shire] artifact hand weapon, bearer must be Bilbo
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GoblinTroopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Goblin Troop
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.<br>While you can spot a [dwarven] artifact, the minion archery total is +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troop");

		assertEquals("Goblin Troop", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayingADwarvenArtifactToASupportAreaAddsOneToTheMinionArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		scn.MoveCardsToHand(arkenstone);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInHand(arkenstone);

		// The Troop is an archer: 1 by the rules alone.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		assertTrue(scn.FreepsPlayAvailable(arkenstone));
		scn.FreepsPlayCard(arkenstone);
		assertInZone(Zone.SUPPORT, arkenstone);
		// 1 for the archer + 1 while a [dwarven] artifact can be spotted.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DwarvenPossessionsAndAShireArtifactAddNothingUntilADwarvenArtifactIsBorne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var thorin = scn.GetFreepsCard("thorin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var ponies = scn.GetFreepsCard("ponies");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(balin, axe);
		scn.AttachCardsTo(scn.GetRingBearer(), sting);
		scn.MoveCardsToSupportArea(ponies);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();
		assertAttachedTo(axe, balin);
		assertAttachedTo(sting, scn.GetRingBearer());
		assertInZone(Zone.SUPPORT, ponies);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		// A [dwarven] possession borne, another in a support area, and a [shire] artifact borne: the archer's 1 alone.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [dwarven] artifact that someone bears is spotted as well as one in a support area.
		scn.AttachCardsTo(thorin, orcrist);
		assertAttachedTo(orcrist, thorin);
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}
}
