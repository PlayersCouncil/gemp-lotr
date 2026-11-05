package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_04_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("archers", "4_61");
					// Arwen, Daughter of Elrond: Elf companion, no archer; her bonus is against a Nazgul only
					put("arwen", "1_30");
					// Haldir, Elf of the Golden Wood: Elf companion, no archer; his bonus is at sites 6 to 8 only
					put("haldir", "1_48");
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an Elf ally; a fellowship ability, never used
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion who is no Elf; a skirmish ability, never used
					// Shoulder to Shoulder: a Free Peoples condition in the support area; a maneuver ability, never used
					put("shoulder", "1_59");

					// Black Breath: a Shadow condition in the support area; its other text reaches only its bearer
					put("breath", "1_207");
					// Weary: a Shadow condition on a companion; its trigger wants an [isengard] tracker
					put("weary", "4_212");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, no archer
					put("scimitar", "1_180");   // Goblin Scimitar: a Shadow possession, on a [moria] Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CompanyofArchersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Company of Archers
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: <b>Archery:</b> Spot 2 Elf companions to discard a condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("archers");

		assertEquals("Company of Archers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoElfCompanionsItDiscardsAConditionOfEitherSideFromWhereverItLies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archers = scn.GetFreepsCard("archers");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var gimli = scn.GetFreepsCard("gimli");
		var shoulder = scn.GetFreepsCard("shoulder");
		var breath = scn.GetShadowCard("breath");
		var weary = scn.GetShadowCard("weary");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(arwen, haldir, gimli);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToHand(archers);

		scn.StartGame();
		scn.MoveCardsToSupportArea(breath);
		scn.AttachCardsTo(gimli, weary);
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, scimitar);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(archers));
		assertInZone(Zone.SUPPORT, breath, shoulder);
		assertAttachedTo(weary, gimli);
		assertAttachedTo(scimitar, runner);

		scn.FreepsPlayCard(archers);
		// Any condition, Shadow or Free Peoples, in a support area or borne; not a possession. One must be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(breath, weary, shoulder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scimitar));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(weary);

		assertInZone(Zone.DISCARD, weary, archers);
		assertInZone(Zone.SUPPORT, breath, shoulder);
		assertAttachedTo(scimitar, runner);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void NotPlayableWithOneElfCompanionThoughAnElfAllyIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archers = scn.GetFreepsCard("archers");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var shoulder = scn.GetFreepsCard("shoulder");
		var breath = scn.GetShadowCard("breath");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToHand(archers);

		scn.StartGame();
		scn.MoveCardsToSupportArea(breath);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ARCHERY);

		// One Elf companion; beside him an Elf who is no companion and a companion who is no Elf.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(archers));
		assertInZone(Zone.SUPPORT, breath, shoulder);
	}
}
