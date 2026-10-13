package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_69");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetMountScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dart", "8_69");
					// Úlairë Lemenya: Nazgûl, strength 9, vitality 2; her text is about a character who bears
					// a [wraith] condition.  Black Steed: a mount for a Nazgûl, strength +2 (and +2 more at a
					// plains site); it is discarded only at an underground site.
					put("lemenya", "1_232");
					put("steed", "1_208");
					put("guard", "5_83");       // Household Guard: [rohan] Man, valiant and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					// Horse of Rohan: mount, cost 2, not unique.  "To play, spot a [rohan] Man.  Bearer must be
					// a Man, Elf, or Wizard."  Its trigger answers the start of a skirmish its bearer is in.
					put("horse1", "4_283");
					put("horse2", "4_283");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackDartStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Black Dart
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Archery:</b> Spot your enduring or mounted Nazgûl to transfer this condition from your support area to an unbound companion. Discard a mount borne by that companion.<br>Bearer cannot heal or bear mounts.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Black Dart", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void BlackDartTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}

	// "Discard a mount borne by that companion" is a standing discard, apart from "Bearer cannot ... bear mounts"
	// (Ketura on Q-0223: Black Dart is read as Anduril is, with "an ongoing discard effect"; Q-0229: the discard
	// is continuous).  The mount is on the companion as Black Dart is transferred to him, and is discarded.
	@Test
	public void TransferredToAMountedCompanionItDiscardsHisMountAndNoOtherCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMountScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var horse1 = scn.GetFreepsCard("horse1");
		var horse2 = scn.GetFreepsCard("horse2");
		var frodo = scn.GetRingBearer();
		var dart = scn.GetShadowCard("dart");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.AttachCardsTo(guard, horse1);
		scn.AttachCardsTo(aragorn, horse2);
		scn.MoveCardsToSupportArea(dart);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		// A mounted Nazgûl
		scn.AttachCardsTo(lemenya, steed);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(dart));
		scn.ShadowUseCardAction(dart);
		// An unbound companion: not the Ring-bearer
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(guard);

		assertAttachedTo(dart, guard);
		assertInZone(Zone.DISCARD, horse1);
		// Another companion's mount, and the Nazgûl's own, are not borne by the Dart's bearer
		assertAttachedTo(horse2, aragorn);
		assertAttachedTo(steed, lemenya);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	// Comprehensive Rules 5.0, "hinder": while Black Dart is hindered its text does not apply, so its bearer may
	// take up a mount.  When it is restored the standing discard is in force again and the mount is discarded
	// (Ketura on Q-0223).  The restore here is the Shadow player's reconcile, which comes in the regroup phase
	// before the Free Peoples player chooses to move or stay.
	@Test
	public void AMountItsBearerTookUpWhileItWasHinderedIsDiscardedWhenItIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMountScenario();

		var guard = scn.GetFreepsCard("guard");
		var horse = scn.GetFreepsCard("horse1");
		var dart = scn.GetShadowCard("dart");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, dart);
		scn.MoveCardsToHand(horse);

		scn.StartGame();

		// "Bearer cannot ... bear mounts": the Guard is the only Man, Elf or Wizard on the table
		assertFalse(scn.FreepsPlayAvailable(horse));

		scn.HinderCard(dart);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(horse));
		scn.FreepsPlayCard(horse);
		assertAttachedTo(horse, guard);
		assertTrue(scn.IsHindered(dart));

		// No minion: the Shadow phase is followed by the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.IsHindered(dart));
		assertAttachedTo(horse, guard);

		scn.PassRegroupActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.IsHindered(dart));
		assertAttachedTo(dart, guard);
		assertInZone(Zone.DISCARD, horse);
	}
}
