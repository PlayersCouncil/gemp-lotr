package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_263_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brego", "4_263");
					put("rider1", "4_286");     // Rider of Rohan: [rohan] Man companion, vitality 3; a skirmish ability used only on purpose
					put("rider2", "4_286");
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally; an archery ability that needs a villager
					put("ridersmount", "4_287"); // Rider's Mount: [rohan] mount, cost 1, bearer must be a [rohan] Man
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, vitality 3; strength +3 against a Nazgul only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: Wizard companion; strength by the races he spots
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("blackrider", "12_161"); // Black Rider: [wraith] Nazgul, fierce, strength 8 +1 for each companion, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BregoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Brego
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: To play, spot a [rohan] Man. Bearer must be a Man, Elf, or Wizard. When you play Brego, you may heal bearer.<br>At the start of each skirmish involving bearer, each minion skirmishing bearer must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brego");

		assertEquals("Brego", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilARohanManIsSpottedAndItsHealMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brego = scn.GetFreepsCard("brego");
		var rider = scn.GetFreepsCard("rider1");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(boromir, merry);
		scn.MoveCardsToHand(brego, rider);

		scn.StartGame();

		// A [gondor] Man and a [rohan] Hobbit: no [rohan] Man to spot. Boromir could bear Brego.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(brego));

		scn.FreepsPlayCard(rider);
		assertInZone(Zone.FREE_CHARACTERS, rider);
		assertTrue(scn.FreepsPlayAvailable(brego));

		scn.AddWoundsToChar(rider, 1);
		assertEquals(1, scn.GetWoundsOn(rider));
		scn.FreepsPlayCard(brego);
		scn.FreepsChooseCard(rider);
		assertAttachedTo(brego, rider);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brego));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsPlayedOnAManAnElfOrAWizardAndMayHealThatBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var brego = scn.GetFreepsCard("brego");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveAlliesToTable(aldor);
		scn.MoveCompanionsToTable(boromir, arwen, gandalf, gimli);
		scn.MoveCardsToHand(brego);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(boromir, 1);

		scn.StartGame();

		// Aldor, an ally, is the one [rohan] Man to spot.
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsPlayAvailable(brego));

		scn.FreepsPlayCard(brego);
		// A Man ally, a Man companion, an Elf and a Wizard; not the Hobbit or the Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aldor, boromir, arwen, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(arwen);
		assertAttachedTo(brego, arwen);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brego));
		scn.FreepsAcceptOptionalTrigger();

		// One wound off the bearer, and none off Boromir.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheHealIsNotOfferedForAnUnwoundedBearerOrWhenAnotherMountIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brego = scn.GetFreepsCard("brego");
		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		scn.MoveCompanionsToTable(rider1, rider2);
		scn.MoveCardsToHand(brego, ridersmount);
		scn.AddWoundsToChar(rider2, 1);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(rider1));
		assertEquals(1, scn.GetWoundsOn(rider2));

		// On a bearer with no wound the heal would do nothing.
		scn.FreepsPlayCard(brego);
		scn.FreepsChooseCard(rider1);
		assertAttachedTo(brego, rider1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another mount played on a wounded [rohan] Man while Brego is in play: not "when you play Brego".
		// Brego's own bearer is wounded now, so that a heal of him would be on offer if it were.
		scn.AddWoundsToChar(rider1, 1);
		assertEquals(1, scn.GetWoundsOn(rider1));
		scn.FreepsPlayCard(ridersmount);
		assertAttachedTo(ridersmount, rider2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(rider1));
		assertEquals(1, scn.GetWoundsOn(rider2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfBearersSkirmishEachMinionSkirmishingHimExertsAndNoOtherSkirmishDoesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brego = scn.GetFreepsCard("brego");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, brego);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, savage, troop2, troop3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, troop1}, new PhysicalCardImpl[]{aragorn, troop2}, new PhysicalCardImpl[]{frodo, troop3});
		// The Uruk Savage was left over: the Shadow player puts him on Boromir as well.
		scn.ShadowAssignToMinions(boromir, savage);

		// Aragorn's skirmish first: nobody exerts, those assigned to Boromir included.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop2));

		scn.PassSkirmishActions();
		// Aragorn, 8 against 13, loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop2));

		// Boromir's skirmish: an Orc and an Uruk-hai, each exerts once; the Troop that fought Aragorn does not,
		// nor the one assigned to Frodo, whose skirmish has not begun.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));
	}

	@Test
	public void AMinionSkirmishingBearerAgainInTheFierceRoundExertsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brego = scn.GetFreepsCard("brego");
		var boromir = scn.GetFreepsCard("boromir");
		var blackrider = scn.GetShadowCard("blackrider");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, brego);

		scn.StartGame();
		scn.MoveMinionsToTable(blackrider);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, blackrider);

		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(blackrider));

		// 8 + 2 companions = 10 against Boromir's 7: Boromir takes a wound and is not overwhelmed.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(blackrider));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, blackrider);
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetWoundsOn(blackrider));
	}
}
