package com.gempukku.lotro.mechanics.inplay;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * "Add 1 to the number of <Ents, pipes> you can spot."
 *
 * "You" is the player whose card says it: the number is raised for his own counts and for no other player's
 * (F-0502). The Shadow player's Urgency 19_17 counts the Ents HE can spot, and the Free Peoples player's Pippin,
 * In the Bloom of Health 17_109 does not add to that count. The engine's modifier answers only the count of its
 * card's owner (RaceSpotModifier, PossessionClassSpotModifier; Filters.countSpottable with the spotting player).
 *
 * No printed card has a player count the pipes another player's card added to: every count of pipes is a Free
 * Peoples card's. The pipe test here shows the count of the player who added the pipe.
 */
public class AddingToTheNumberYouCanSpotTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Pippin, In the Bloom of Health: [shire] companion. "Add 1 to the number of Ents you can
					// spot. Maneuver: Discard 2 pipeweed cards from play to remove a burden." The ability is not used.
					put("pippin", "17_109");
					// Huorn: [gandalf] Tree companion, strength 6, "strength +1 for each Ent you can spot". He is
					// no Ent; his response is not used.
					put("huorn", "9_25");
					// Ent Avenger: [gandalf] Ent companion, "While you can spot 3 Ents, this companion is
					// strength +2".
					put("avenger", "6_27");
					// Urgency: [isengard] support-area condition. "Each time the fellowship moves, add (2) for
					// each Ent you can spot." Its skirmish ability is not used.
					put("urgency", "19_17");

					// Gimli's Pipe: "Fellowship: Exert Gimli to add 1 to the number of pipes you can spot."
					put("gimlispipe", "3_2");
					put("gimli", "1_13");       // Gimli, Son of Gloin: damage +1; his skirmish ability is not used
					// Gandalf's Pipe: "Fellowship: Discard a pipeweed possession and spot X pipes to remove X burdens."
					put("gandalfspipe", "1_74");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: no ability
					put("toby", "1_305");       // Old Toby: a pipeweed possession in the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheAddedEntIsCountedByItsOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var huorn = scn.GetFreepsCard("huorn");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCardsToHand(pippin);
		scn.MoveCompanionsToTable(huorn, avenger);

		scn.StartGame();
		// One Ent: Huorn 6 + 1.
		assertEquals(7, scn.GetStrength(huorn));

		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		// One Ent and Pippin's 1.
		assertEquals(8, scn.GetStrength(huorn));
	}

	@Test
	public void TheAddedEntIsNotCountedByTheOtherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var avenger = scn.GetFreepsCard("avenger");
		var urgency = scn.GetShadowCard("urgency");
		scn.MoveCompanionsToTable(pippin, avenger);
		scn.MoveCardsToSupportArea(urgency);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 2 for Ettenmoors + 3 companions + 2 for the one Ent the Shadow player can spot. With Pippin's 1 in
		// his count it would be 4 for Urgency, 9 in all.
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheAddedPipeIsCountedByThePlayerWhoAddedIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var gimlispipe = scn.GetFreepsCard("gimlispipe");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli, gandalf);
		scn.AttachCardsTo(gimli, gimlispipe);
		scn.AttachCardsTo(gandalf, gandalfspipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.AddBurdens(3);

		scn.FreepsUseCardAction(gimlispipe);
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.FreepsUseCardAction(gandalfspipe);
		assertInZone(Zone.DISCARD, toby);
		// Two pipes on the table and Gimli's Pipe's 1.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.FreepsChoose("3");
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
