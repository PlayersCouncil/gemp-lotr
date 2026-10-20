package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "1_14");
					put("gimli", "0_62");       // Gimli, Dwarven Delegate: strength 7, vitality 3, damage +1 and no other text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("veteran", "17_86");    // Orkish Veteran: [orc] Orc, strength 7, vitality 4 (lives through three wounds)
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only: no Orc
					put("savage2", "1_151");
					put("shelob", "8_25");      // Shelob, Eater of Light: [gollum] Spider, strength 8, vitality 8, fierce
					                            // (cheated in, so her play trigger never fires)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "1_14");
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, damage +1
					put("farin", "1_11");       // Farin, Dwarven Emissary: a [dwarven] Dwarf companion who is not Gimli, no damage bonus
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimlisBattleAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gimli's Battle Axe
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Gimli.
		 * He is damage +1.
		 * Each time Gimli wins a skirmish, you may wound an Orc.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Gimli's Battle Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void CannotBePlayedOnAnotherDwarfAndOnGimliMakesHimDamagePlusOneMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(farin);
		scn.MoveCardsToHand(axe, gimli);

		scn.StartGame();

		// Farin is a Dwarf companion, and no Gimli.
		assertFalse(scn.FreepsPlayAvailable(axe));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(axe));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));

		// Gimli is the one bearer it may go on, so it goes on him unasked.
		scn.FreepsPlayCard(axe);
		assertAttachedTo(axe, gimli);
		assertNotAttachedTo(axe, farin);
		// His own damage +1 and the axe's.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeGimliWinsASkirmishHeMayWoundAnOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var veteran = scn.GetShadowCard("veteran");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, troop, savage, shelob);
		scn.SkipToAssignments();
		// The Troop, the Savage and Shelob are assigned to nobody.
		scn.FreepsAssignToMinions(gimli, veteran);
		scn.ShadowDeclineAssignments();

		// Gimli 7 + 2 for the axe against the Veteran's 7: he wins, and the Veteran takes
		// 1 wound for losing + 1 for Gimli's own damage +1 + 1 for the axe's.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(axe));

		scn.FreepsAcceptOptionalTrigger();
		// An Orc of two cultures, the one he beat and one in no skirmish; the Uruk-hai and the Spider are no Orcs.
		assertTrue(scn.FreepsHasCardChoiceAvailable(veteran, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, shelob, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(3, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// The fierce round: Gimli's 9 beats Shelob's 8, a second win, and the wound is offered again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, shelob);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(shelob));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(axe));

		// Declined, nothing more is wounded.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(shelob));
		assertEquals(3, scn.GetWoundsOn(veteran));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAnotherCompanionWinsOrWhenGimliLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(gimli, axe);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, troop});

		// Aragorn 8 against the Savage's 5: another companion wins, with the Troop, an Orc, there to be wounded.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(axe));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Gimli 9 against the Troop's 13: he loses (not overwhelmed), with the Troop still there to be wounded.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(axe));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenGimliWinsWithNoOrcInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.ShadowDeclineAssignments();

		// Gimli 9 against the Savage's 5: he wins, and three wounds kill the Savage. The other Savage, a minion
		// but no Orc, is left in play: nothing an Orc-wound could be put on.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(axe));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
